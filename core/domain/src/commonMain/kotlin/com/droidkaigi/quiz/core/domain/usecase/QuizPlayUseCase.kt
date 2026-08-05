package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.Answer
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.QuizSession
import com.droidkaigi.quiz.core.domain.scoring.QuizScorer
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import kotlin.coroutines.cancellation.CancellationException

/**
 * Aggregates answer submit → completion check → scoring → ranking upload.
 *
 * UI feedback timing (tap-to-continue, any fixed presentation delay) stays in the presentation
 * layer and must not call into this use case until the user continues — so delays never change
 * [QuizSessionStore.finishedAtEpochMillis] / timeBonus.
 */
class QuizPlayUseCase(
    private val quizEngine: QuizEngine,
    private val sessionStore: QuizSessionStore,
    private val submitScoreUseCase: SubmitScoreUseCase,
    private val instantProvider: InstantProvider,
) {
    /**
     * Records [answer] for the current question and advances the session.
     * Rejects stale / double-tap answers whose [Answer.questionId] is not the current question.
     */
    fun submitAnswer(answer: Answer): SubmitQuizAnswerResult {
        val session = sessionStore.currentSession ?: return SubmitQuizAnswerResult.Rejected
        if (session.isComplete) return SubmitQuizAnswerResult.Rejected
        val question = session.currentQuestion ?: return SubmitQuizAnswerResult.Rejected
        if (answer.questionId != question.id) return SubmitQuizAnswerResult.Rejected
        if (question.id in session.answers) return SubmitQuizAnswerResult.Rejected

        val isCorrect = QuizScorer.isCorrect(question, answer)
        var updated = quizEngine.submitAnswer(session, answer)
        updated = quizEngine.advance(updated)
        sessionStore.currentSession = updated
        if (updated.isComplete) {
            sessionStore.finishedAtEpochMillis = instantProvider.nowEpochMillis()
        }
        return SubmitQuizAnswerResult.Accepted(
            isCorrect = isCorrect,
            session = updated,
            isComplete = updated.isComplete,
        )
    }

    /**
     * Scores a completed session (once) and uploads the score.
     * Safe to call again after [CompleteQuizResult.Failure] (retry) or while Ignored when in-flight.
     */
    suspend fun completeAndSubmitScore(): CompleteQuizResult {
        val session = sessionStore.currentSession ?: return CompleteQuizResult.Ignored
        if (!session.isComplete) return CompleteQuizResult.Ignored
        if (sessionStore.scoreSubmitInFlight) return CompleteQuizResult.Ignored

        val finishedAt = sessionStore.finishedAtEpochMillis
            ?: instantProvider.nowEpochMillis().also { sessionStore.finishedAtEpochMillis = it }
        val result = sessionStore.pendingResult
            ?: QuizScorer.scoreSession(session, finishedAt).also {
                sessionStore.pendingResult = it
                sessionStore.lastResult = it
            }

        sessionStore.scoreSubmitInFlight = true
        return try {
            submitScoreUseCase(
                result = result,
                folderId = session.folderId,
                completedAtEpochMillis = finishedAt,
                startedAtEpochMillis = session.startedAtEpochMillis,
            )
            sessionStore.scoreSubmitInFlight = false
            CompleteQuizResult.Success(result)
        } catch (e: CancellationException) {
            sessionStore.scoreSubmitInFlight = false
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
            sessionStore.scoreSubmitInFlight = false
            CompleteQuizResult.Failure(result = result, canRetry = true)
        }
    }
}

sealed interface SubmitQuizAnswerResult {
    data class Accepted(
        val isCorrect: Boolean,
        val session: QuizSession,
        val isComplete: Boolean,
    ) : SubmitQuizAnswerResult

    /** No session, already complete, wrong question, or already answered. */
    data object Rejected : SubmitQuizAnswerResult
}

sealed interface CompleteQuizResult {
    data class Success(val result: QuizResult) : CompleteQuizResult

    data class Failure(
        val result: QuizResult,
        val canRetry: Boolean,
    ) : CompleteQuizResult

    /** Not complete, missing session, or submit already in flight. */
    data object Ignored : CompleteQuizResult
}
