package jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz

import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.QuizSessionHolder
import jp.co.yumemi.quiz.droidkaigi.core.data.SiteStatusHolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ChoiceOption
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffSession
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.LocalStaffAppVersionProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAppReleaseRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAuthRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.session.QuizEngine
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.CheckForStaffAppUpdateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ClearTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.CreateQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.DeleteQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.DeleteRankingEntryUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.DownloadStaffAppUpdateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetActiveQuizFolderIdUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetQuizSetForFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetStaffAuthStateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ListQuizFoldersUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ObserveTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuickSignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuizPlayUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.RestoreStaffAuthSessionUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SaveQuizSetUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SetActiveQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignOutStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.StaffAuthSessionStore
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SubmitScoreUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.UpdateQuizFolderUseCase
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
        override suspend fun getSitePublished(): Boolean = fail("unused")
        override suspend fun setSitePublished(published: Boolean) = fail("unused")
        override fun observeAppConfig() =
            kotlinx.coroutines.flow.emptyFlow<jp.co.yumemi.quiz.droidkaigi.core.domain.model.AppConfigStatus>()
    }

    private fun unusedStaffRepo(): StaffAuthRepository = object : StaffAuthRepository {
        override suspend fun signIn(email: String, password: String): Result<StaffSession> = fail("unused")
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
            quizCatalogRepository = catalog,
            quizEngine = quizEngine,
            sessionHolder = sessionHolder,
            siteStatusHolder = SiteStatusHolder(),
            quizPlayUseCase = QuizPlayUseCase(
                quizEngine = quizEngine,
                sessionStore = sessionHolder,
                submitScoreUseCase = submitScoreUseCase,
                instantProvider = instantProvider,
            ),
            getTodayRankingsUseCase = GetTodayRankingsUseCase(rankingRepository),
            observeTodayRankingsUseCase = ObserveTodayRankingsUseCase(rankingRepository),
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
            restoreStaffAuthSessionUseCase = RestoreStaffAuthSessionUseCase(staffRepo, staffStore),
            getStaffAuthStateUseCase = GetStaffAuthStateUseCase(staffStore),
            signOutStaffUseCase = SignOutStaffUseCase(staffStore, staffRepo),
            checkForStaffAppUpdateUseCase = CheckForStaffAppUpdateUseCase(
                staffAppReleaseRepository = object : StaffAppReleaseRepository {
                    override suspend fun fetchLatestRelease() = null
                    override suspend fun downloadDmg(
                        release: jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppRelease,
                        onProgress: (Long, Long?) -> Unit,
                    ) = Result.failure<String>(UnsupportedOperationException())
                    override fun openDownloadedFile(path: String) = Unit
                },
                localStaffAppVersionProvider = object : LocalStaffAppVersionProvider {
                    override fun current() =
                        jp.co.yumemi.quiz.droidkaigi.core.domain.model.LocalStaffAppVersion("1.0.0", 10_000)
                },
            ),
            downloadStaffAppUpdateUseCase = DownloadStaffAppUpdateUseCase(
                staffAppReleaseRepository = object : StaffAppReleaseRepository {
                    override suspend fun fetchLatestRelease() = null
                    override suspend fun downloadDmg(
                        release: jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppRelease,
                        onProgress: (Long, Long?) -> Unit,
                    ) = Result.failure<String>(UnsupportedOperationException())
                    override fun openDownloadedFile(path: String) = Unit
                },
            ),
        )
    }
}
