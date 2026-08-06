package com.droidkaigi.quiz.feature.quiz.quiz

import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.data.QuizSessionHolder
import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.usecase.ClearTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.CreateQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteRankingEntryUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetActiveQuizFolderIdUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetQuizSetForFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetStaffAuthStateUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.ListQuizFoldersUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuickSignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuizPlayUseCase
import com.droidkaigi.quiz.core.domain.usecase.SaveQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetActiveQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignOutStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.StaffAuthSessionStore
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import com.droidkaigi.quiz.core.domain.usecase.UpdateQuizFolderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.coroutines.cancellation.CancellationException
import kotlin.coroutines.resume
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertTrue
import kotlin.test.fail

@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelSubmitScoreTest {
    private val dispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(dispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun submitScore_failure_showsFailedPhase_andRetrySucceedsWithFixedCompletedAt() = runTest(dispatcher) {
        val ranking = ControllableRankingRepository()
        ranking.nextFailures = 1
        val sessionHolder = QuizSessionHolder()
        val clock = MutableInstantProvider(1_700_000_000_000L)
        val deps = testAppDependencies(ranking, clock, sessionHolder)
        val viewModel = createViewModelAtFinalFeedback(deps, sessionHolder, clock)
        val finishedAt = clock.millis

        clock.millis = finishedAt + 60_000L
        viewModel.onIntent(QuizIntent.ContinueAfterFeedback)
        advanceUntilIdle()
        assertEquals(SubmitPhase.Failed, viewModel.uiState.value.submitPhase)
        assertEquals(1, ranking.submitCount)
        assertEquals(finishedAt, ranking.completedAts.single())

        clock.millis = finishedAt + 120_000L
        val navigate = async { viewModel.events.first() }
        viewModel.onIntent(QuizIntent.RetrySubmitScore)
        advanceUntilIdle()

        assertEquals(QuizEvent.NavigateToResult, navigate.await())
        assertEquals(SubmitPhase.Idle, viewModel.uiState.value.submitPhase)
        assertEquals(2, ranking.submitCount)
        assertEquals(listOf(finishedAt, finishedAt), ranking.completedAts)
        assertEquals(1, ranking.scores.distinct().size)
        assertEquals(sessionHolder.lastResult?.score, ranking.scores.first())
    }

    @Test
    fun submitScore_whileSubmitting_ignoresDuplicateContinueAndRetry() = runTest(dispatcher) {
        val ranking = ControllableRankingRepository(blockUntilReleased = true)
        val sessionHolder = QuizSessionHolder()
        val clock = MutableInstantProvider(1_700_000_000_000L)
        val deps = testAppDependencies(ranking, clock, sessionHolder)
        val viewModel = createViewModelAtFinalFeedback(deps, sessionHolder, clock)

        viewModel.onIntent(QuizIntent.ContinueAfterFeedback)
        advanceUntilIdle()
        assertEquals(SubmitPhase.Submitting, viewModel.uiState.value.submitPhase)

        viewModel.onIntent(QuizIntent.ContinueAfterFeedback)
        viewModel.onIntent(QuizIntent.RetrySubmitScore)
        advanceUntilIdle()
        assertEquals(1, ranking.submitCount)

        val navigate = async { viewModel.events.first() }
        ranking.release()
        advanceUntilIdle()
        assertEquals(QuizEvent.NavigateToResult, navigate.await())
        assertEquals(1, ranking.submitCount)
    }

    @Test
    fun submitScore_cancellation_isNotConvertedToFailed() = runTest(dispatcher) {
        val ranking = ControllableRankingRepository(throwCancellation = true)
        val sessionHolder = QuizSessionHolder()
        val clock = MutableInstantProvider(1_700_000_000_000L)
        val deps = testAppDependencies(ranking, clock, sessionHolder)
        val viewModel = createViewModelAtFinalFeedback(deps, sessionHolder, clock)

        viewModel.onIntent(QuizIntent.ContinueAfterFeedback)
        advanceUntilIdle()

        assertNotEquals(SubmitPhase.Failed, viewModel.uiState.value.submitPhase)
        assertTrue(
            viewModel.uiState.value.submitPhase == SubmitPhase.Submitting ||
                viewModel.uiState.value.submitPhase == SubmitPhase.Idle,
        )
    }

    @Test
    fun viewModelRecreation_keepsFinishedAtForRetry() = runTest(dispatcher) {
        val ranking = ControllableRankingRepository()
        ranking.nextFailures = 1
        val sessionHolder = QuizSessionHolder()
        val clock = MutableInstantProvider(1_700_000_000_000L)
        val deps = testAppDependencies(ranking, clock, sessionHolder)
        val firstVm = createViewModelAtFinalFeedback(deps, sessionHolder, clock)
        val finishedAt = checkNotNull(sessionHolder.finishedAtEpochMillis)

        firstVm.onIntent(QuizIntent.ContinueAfterFeedback)
        advanceUntilIdle()
        assertEquals(SubmitPhase.Failed, firstVm.uiState.value.submitPhase)

        clock.millis = finishedAt + 90_000L
        val recreated = QuizViewModel(deps)
        assertEquals(true, recreated.uiState.value.isFinishing)
        assertEquals(true, recreated.uiState.value.lastAnswerCorrect)
        assertEquals(SubmitPhase.Failed, recreated.uiState.value.submitPhase)
        assertEquals(finishedAt, sessionHolder.finishedAtEpochMillis)

        val navigate = async { recreated.events.first() }
        recreated.onIntent(QuizIntent.RetrySubmitScore)
        advanceUntilIdle()
        assertEquals(QuizEvent.NavigateToResult, navigate.await())
        assertEquals(listOf(finishedAt, finishedAt), ranking.completedAts)
    }

    @Test
    fun viewModelRecreation_beforeSubmit_restoresFeedbackContinue() = runTest(dispatcher) {
        val ranking = ControllableRankingRepository()
        val sessionHolder = QuizSessionHolder()
        val clock = MutableInstantProvider(1_700_000_000_000L)
        val deps = testAppDependencies(ranking, clock, sessionHolder)
        createViewModelAtFinalFeedback(deps, sessionHolder, clock)
        val finishedAt = checkNotNull(sessionHolder.finishedAtEpochMillis)

        val recreated = QuizViewModel(deps)
        assertEquals(true, recreated.uiState.value.showFeedback)
        assertEquals(true, recreated.uiState.value.lastAnswerCorrect)
        assertEquals(SubmitPhase.Idle, recreated.uiState.value.submitPhase)

        val navigate = async { recreated.events.first() }
        recreated.onIntent(QuizIntent.ContinueAfterFeedback)
        advanceUntilIdle()
        assertEquals(QuizEvent.NavigateToResult, navigate.await())
        assertEquals(listOf(finishedAt), ranking.completedAts)
    }

    private fun createViewModelAtFinalFeedback(
        deps: AppDependencies,
        sessionHolder: QuizSessionHolder,
        clock: MutableInstantProvider,
    ): QuizViewModel {
        val question = SingleChoice(
            id = "q1",
            prompt = "Q",
            options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B")),
            correctId = "a",
        )
        sessionHolder.beginSession(
            deps.quizEngine.startSession(
                folderId = "folder",
                quizSet = QuizSet("folder", "Demo", listOf(question)),
                nickname = "Alice",
                startedAtEpochMillis = clock.millis,
            ),
        )
        val viewModel = QuizViewModel(deps)
        viewModel.onIntent(QuizIntent.SelectSingle("a"))
        viewModel.onIntent(QuizIntent.SubmitAnswer)
        assertEquals(true, viewModel.uiState.value.isFinishing)
        assertEquals(true, viewModel.uiState.value.showFeedback)
        return viewModel
    }

    private class MutableInstantProvider(var millis: Long) : InstantProvider {
        override fun nowEpochMillis(): Long = millis
    }

    private class ControllableRankingRepository(
        private val blockUntilReleased: Boolean = false,
        private val throwCancellation: Boolean = false,
    ) : RankingRepository {
        var nextFailures: Int = 0
        var submitCount: Int = 0
        val completedAts = mutableListOf<Long>()
        val scores = mutableListOf<Int>()
        private var releaseCont: ((Unit) -> Unit)? = null

        fun release() {
            releaseCont?.invoke(Unit)
            releaseCont = null
        }

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
            if (blockUntilReleased) {
                suspendCancellableCoroutine { cont ->
                    releaseCont = { cont.resume(Unit) }
                }
            }
        }

        override suspend fun deleteEntry(folderId: String, entryId: String) = Unit

        override suspend fun clearTodayRankings(folderId: String) = Unit
    }

    private fun unusedCatalog(): QuizCatalogRepository = object : QuizCatalogRepository {
        override suspend fun listFolders(): List<QuizFolder> = fail("unused")
        override suspend fun createFolder(name: String, description: String): QuizFolder = fail("unused")
        override suspend fun updateFolder(folder: QuizFolder) = fail("unused")
        override suspend fun deleteFolder(folderId: String) = fail("unused")
        override suspend fun getQuizSet(folderId: String): QuizSet = fail("unused")
        override suspend fun saveQuizSet(quizSet: QuizSet) = fail("unused")
        override suspend fun getActiveFolderId(): String = fail("unused")
        override suspend fun setActiveFolderId(folderId: String) = fail("unused")
    }

    private fun unusedStaffRepo(): StaffAuthRepository = object : StaffAuthRepository {
        override suspend fun signIn(email: String, password: String): Result<StaffSession> =
            fail("unused")
    }

    private fun unusedSessionStore(): StaffAuthSessionStore = object : StaffAuthSessionStore {
        override var currentSession: StaffSession? = null
    }

    private fun testAppDependencies(
        rankingRepository: RankingRepository,
        instantProvider: InstantProvider,
        sessionHolder: QuizSessionHolder,
    ): AppDependencies {
        val catalog = unusedCatalog()
        val staffRepo = unusedStaffRepo()
        val staffStore = unusedSessionStore()
        val signIn = SignInStaffUseCase(staffRepo, staffStore)
        val quizEngine = QuizEngine()
        val submitScoreUseCase = SubmitScoreUseCase(rankingRepository)
        return AppDependencies(
            instantProvider = instantProvider,
            quizEngine = quizEngine,
            sessionHolder = sessionHolder,
            submitScoreUseCase = submitScoreUseCase,
            quizPlayUseCase = QuizPlayUseCase(
                quizEngine = quizEngine,
                sessionStore = sessionHolder,
                submitScoreUseCase = submitScoreUseCase,
                instantProvider = instantProvider,
            ),
            getTodayRankingsUseCase = GetTodayRankingsUseCase(rankingRepository),
            deleteRankingEntryUseCase = DeleteRankingEntryUseCase(rankingRepository),
            clearTodayRankingsUseCase = ClearTodayRankingsUseCase(rankingRepository),
            listQuizFoldersUseCase = ListQuizFoldersUseCase(catalog),
            createQuizFolderUseCase = CreateQuizFolderUseCase(catalog),
            updateQuizFolderUseCase = UpdateQuizFolderUseCase(catalog),
            deleteQuizFolderUseCase = DeleteQuizFolderUseCase(catalog),
            getQuizSetForFolderUseCase = GetQuizSetForFolderUseCase(catalog),
            saveQuizSetUseCase = SaveQuizSetUseCase(catalog),
            getActiveQuizFolderIdUseCase = GetActiveQuizFolderIdUseCase(catalog),
            setActiveQuizFolderUseCase = SetActiveQuizFolderUseCase(catalog),
            signInStaffUseCase = signIn,
            quickSignInStaffUseCase = QuickSignInStaffUseCase(staffRepo, signIn),
            getStaffAuthStateUseCase = GetStaffAuthStateUseCase(staffStore),
            signOutStaffUseCase = SignOutStaffUseCase(staffStore),
        )
    }
}
