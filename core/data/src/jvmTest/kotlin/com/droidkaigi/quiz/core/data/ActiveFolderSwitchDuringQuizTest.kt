package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.domain.model.SingleChoiceAnswer
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.usecase.CompleteQuizResult
import com.droidkaigi.quiz.core.domain.usecase.GetActiveQuizFolderIdUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetQuizSetForFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuizPlayUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetActiveQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue

/**
 * Staff may publish another folder while a participant is mid-quiz.
 * The running session is a snapshot, so questions must not swap and the score must
 * still land in the folder the participant actually played.
 */
class ActiveFolderSwitchDuringQuizTest {
    private val clock = object : InstantProvider {
        override fun nowEpochMillis(): Long = 1_700_000_000_000L
    }

    @Test
    fun publishingAnotherFolderMidQuiz_keepsQuestionsAndRanksInPlayedFolder() = runBlocking {
        val catalog = InMemoryQuizCatalog()
        catalog.withLock {
            seedFolder(folder = folder(DAY1), quizSet = quizSet(DAY1))
            seedFolder(folder = folder(DAY2, sortOrder = 1), quizSet = quizSet(DAY2))
            setActiveFolderId(DAY1)
        }
        val catalogRepository = InMemoryQuizCatalogRepository(catalog, clock)
        val rankingRepository = FakeRankingRepository(clock, catalog)
        val sessionHolder = QuizSessionHolder()
        val quizPlayUseCase = QuizPlayUseCase(
            quizEngine = QuizEngine(),
            sessionStore = sessionHolder,
            submitScoreUseCase = SubmitScoreUseCase(rankingRepository),
            instantProvider = clock,
        )

        val playedFolderId = GetActiveQuizFolderIdUseCase(catalogRepository)()
        val playedQuizSet = GetQuizSetForFolderUseCase(catalogRepository)(playedFolderId)
        sessionHolder.beginSession(
            QuizEngine().startSession(
                folderId = playedFolderId,
                quizSet = playedQuizSet,
                nickname = "Alice",
                startedAtEpochMillis = clock.nowEpochMillis(),
            ),
        )
        quizPlayUseCase.submitAnswer(SingleChoiceAnswer("$DAY1-q1", "a"))

        SetActiveQuizFolderUseCase(catalogRepository)(DAY2)

        val session = requireNotNull(sessionHolder.currentSession)
        assertEquals(DAY1, session.folderId)
        assertEquals(quizSet(DAY1).questions, session.quizSet.questions)
        assertEquals("$DAY1-q2", session.currentQuestion?.id)

        quizPlayUseCase.submitAnswer(SingleChoiceAnswer("$DAY1-q2", "a"))
        assertIs<CompleteQuizResult.Success>(quizPlayUseCase.completeAndSubmitScore())

        val getTodayRankings = GetTodayRankingsUseCase(rankingRepository)
        assertEquals(listOf("Alice"), getTodayRankings(DAY1).map { it.nickname })
        assertTrue(getTodayRankings(DAY2).isEmpty())
        // Ranking screen reads playbackFolderId, so the participant sees the list they are on.
        assertEquals(DAY1, sessionHolder.playbackFolderId)
        assertEquals(DAY2, catalogRepository.getActiveFolderId())
    }

    private fun folder(id: String, sortOrder: Int = 0) =
        QuizFolder(id = id, name = id, description = "", sortOrder = sortOrder)

    private fun quizSet(id: String) = QuizSet(
        id = id,
        title = id,
        questions = listOf("q1", "q2").map { suffix ->
            SingleChoice(
                id = "$id-$suffix",
                prompt = "$id $suffix",
                options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B")),
                correctId = "a",
            )
        },
    )

    private companion object {
        const val DAY1 = "day1"
        const val DAY2 = "day2"
    }
}
