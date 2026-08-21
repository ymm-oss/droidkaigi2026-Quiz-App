package jp.co.yumemi.quiz.droidkaigi.core.domain.session

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Answer
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSession
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet

class QuizEngine {
    fun startSession(folderId: String, quizSet: QuizSet, nickname: String, startedAtEpochMillis: Long): QuizSession =
        QuizSession(
            folderId = folderId,
            quizSet = quizSet,
            nickname = nickname.trim(),
            startedAtEpochMillis = startedAtEpochMillis,
        )

    fun submitAnswer(session: QuizSession, answer: Answer): QuizSession {
        val updated = session.answers + (answer.questionId to answer)
        return session.copy(answers = updated)
    }

    fun advance(session: QuizSession): QuizSession = session.copy(currentIndex = session.currentIndex + 1)
}
