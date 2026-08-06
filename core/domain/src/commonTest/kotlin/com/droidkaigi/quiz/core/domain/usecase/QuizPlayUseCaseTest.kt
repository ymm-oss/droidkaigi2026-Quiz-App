package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.model.QuizSession
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.domain.model.SingleChoiceAnswer
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.cancellation.CancellationException
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class QuizPlayUseCaseTest {
    private val question = SingleChoice(
        id = "q1",
        prompt = "Q",
        options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B")),
        correctId = "a",
    )
    private val quizSet = QuizSet(id = "folder", title = "Demo", questions = listOf(question))

    @Test
    fun submitAnswer_acceptsThenRejectsDoubleTap() {
        val store = InMemoryQuizSessionStore()
        val clock = MutableClock(1_000L)
        val useCase = newUseCase(store, ControllableRankingRepository(), clock)
        store.beginSession(
            QuizEngine().startSession(
                folderId = "folder",
                quizSet = quizSet,
                nickname = "Alice",
                startedAtEpochMillis = clock.millis,
            ),
        )

        val first = useCase.submitAnswer(SingleChoiceAnswer("q1", "a"))
        assertIs<SubmitQuizAnswerResult.Accepted>(first)
        assertTrue(first.isComplete)
        assertEquals(1_000L, store.finishedAtEpochMillis)

        val second = useCase.submitAnswer(SingleChoiceAnswer("q1", "b"))
        assertEquals(SubmitQuizAnswerResult.Rejected, second)
        assertEquals(1_000L, store.finishedAtEpochMillis)
    }

    @Test
    fun completeAndSubmitScore_failureThenRetryKeepsFinishedAt() = runBlocking {
        val store = InMemoryQuizSessionStore()
        val ranking = ControllableRankingRepository()
        ranking.nextFailures = 1
        val clock = MutableClock(1_000L)
        val useCase = newUseCase(store, ranking, clock)
        store.beginSession(
            QuizEngine().startSession(
                folderId = "folder",
                quizSet = quizSet,
                nickname = "Alice",
                startedAtEpochMillis = 500L,
            ),
        )
        assertIs<SubmitQuizAnswerResult.Accepted>(
            useCase.submitAnswer(SingleChoiceAnswer("q1", "a")),
        )

        clock.millis = 5_000L
        val failed = useCase.completeAndSubmitScore()
        assertIs<CompleteQuizResult.Failure>(failed)
        assertEquals(1_000L, store.finishedAtEpochMillis)
        assertEquals(listOf(1_000L), ranking.completedAts)

        clock.millis = 9_000L
        val success = useCase.completeAndSubmitScore()
        assertIs<CompleteQuizResult.Success>(success)
        assertEquals(listOf(1_000L, 1_000L), ranking.completedAts)
        assertEquals(1, ranking.scores.distinct().size)
        assertEquals(store.lastResult?.score, ranking.scores.first())
    }

    @Test
    fun completeAndSubmitScore_ignoresWhileInFlight() = runBlocking {
        val store = InMemoryQuizSessionStore()
        val ranking = ControllableRankingRepository()
        val clock = MutableClock(1_000L)
        val useCase = newUseCase(store, ranking, clock)
        store.beginSession(
            QuizEngine().startSession(
                folderId = "folder",
                quizSet = quizSet,
                nickname = "Alice",
                startedAtEpochMillis = 500L,
            ),
        )
        useCase.submitAnswer(SingleChoiceAnswer("q1", "a"))
        store.scoreSubmitInFlight = true

        assertEquals(CompleteQuizResult.Ignored, useCase.completeAndSubmitScore())
        assertEquals(0, ranking.submitCount)
    }

    @Test
    fun completeAndSubmitScore_cancellationPropagatesAndClearsInFlight() = runBlocking {
        val store = InMemoryQuizSessionStore()
        val ranking = ControllableRankingRepository(throwCancellation = true)
        val clock = MutableClock(1_000L)
        val useCase = newUseCase(store, ranking, clock)
        store.beginSession(
            QuizEngine().startSession(
                folderId = "folder",
                quizSet = quizSet,
                nickname = "Alice",
                startedAtEpochMillis = 500L,
            ),
        )
        useCase.submitAnswer(SingleChoiceAnswer("q1", "a"))

        try {
            useCase.completeAndSubmitScore()
            error("expected CancellationException")
        } catch (_: CancellationException) {
            // expected
        }
        assertEquals(false, store.scoreSubmitInFlight)
        assertNotNull(store.pendingResult)
        Unit
    }

    @Test
    fun beginSession_clearsPriorFinishState() {
        val store = InMemoryQuizSessionStore()
        store.finishedAtEpochMillis = 99L
        store.pendingResult = QuizResult("x", 0, 1, 0, 0)
        store.scoreSubmitInFlight = true
        store.beginSession(
            QuizSession(
                folderId = "folder",
                quizSet = quizSet,
                nickname = "Bob",
                startedAtEpochMillis = 1L,
            ),
        )
        assertNull(store.finishedAtEpochMillis)
        assertNull(store.pendingResult)
        assertEquals(false, store.scoreSubmitInFlight)
        assertEquals("Bob", store.highlightNickname)
    }

    @Test
    fun completeAndSubmitScore_withoutSubmitDoesNotCallRanking() = runBlocking {
        val store = InMemoryQuizSessionStore()
        val ranking = ControllableRankingRepository()
        val clock = MutableClock(1_000L)
        val useCase = newUseCase(store, ranking, clock)
        store.beginSession(
            QuizEngine().startSession(
                folderId = "folder",
                quizSet = quizSet,
                nickname = "Preview",
                startedAtEpochMillis = 500L,
            ),
        )
        useCase.submitAnswer(SingleChoiceAnswer("q1", "a"))

        val result = useCase.completeAndSubmitScore(submitScore = false)
        assertIs<CompleteQuizResult.Success>(result)
        assertEquals(0, ranking.submitCount)
        assertNotNull(store.lastResult)
        assertNull(store.pendingResult)
    }

    private fun newUseCase(
        store: QuizSessionStore,
        ranking: RankingRepository,
        clock: InstantProvider,
    ) = QuizPlayUseCase(
        quizEngine = QuizEngine(),
        sessionStore = store,
        submitScoreUseCase = SubmitScoreUseCase(ranking),
        instantProvider = clock,
    )

    private class MutableClock(var millis: Long) : InstantProvider {
        override fun nowEpochMillis(): Long = millis
    }

    private class InMemoryQuizSessionStore : QuizSessionStore {
        override var currentSession: QuizSession? = null
        override var lastResult: QuizResult? = null
        override var highlightNickname: String? = null
        override var playbackFolderId: String? = null
        override var finishedAtEpochMillis: Long? = null
        override var pendingResult: QuizResult? = null
        override var scoreSubmitInFlight: Boolean = false
    }

    private class ControllableRankingRepository(
        private val throwCancellation: Boolean = false,
    ) : RankingRepository {
        var nextFailures: Int = 0
        var submitCount: Int = 0
        val completedAts = mutableListOf<Long>()
        val scores = mutableListOf<Int>()

        override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = emptyList()

        override suspend fun submitScore(
            result: QuizResult,
            completedAtEpochMillis: Long,
            folderId: String,
            entryId: String,
        ) {
            submitCount += 1
            completedAts += completedAtEpochMillis
            scores += result.score
            if (throwCancellation) {
                throw CancellationException("cancelled submit")
            }
            if (nextFailures > 0) {
                nextFailures -= 1
                error("network down")
            }
        }
    }
}
