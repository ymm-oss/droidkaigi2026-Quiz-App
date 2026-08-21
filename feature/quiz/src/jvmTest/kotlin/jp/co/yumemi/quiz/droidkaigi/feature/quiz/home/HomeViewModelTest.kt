package jp.co.yumemi.quiz.droidkaigi.feature.quiz.home

import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.QuizSessionHolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
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
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.fail

/**
 * 受付状況（sitePublished）の取得失敗を「受付前」に丸めないことを検証する。
 * https://github.com/ymm-oss/droidkaigi2026-Quiz-App/issues/76
 */
@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {
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
    fun shown_fetchFails_marksStatusCheckFailed_insteadOfClosed() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.sitePublishedResults += Result.failure(IllegalStateException("network down"))
        val viewModel = HomeViewModel(testAppDependencies(catalog))

        viewModel.onIntent(HomeIntent.Shown)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.sitePublished)
        assertEquals(true, state.siteStatusCheckFailed)
    }

    @Test
    fun retry_afterFailure_recoversToPublished() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.sitePublishedResults += Result.failure(IllegalStateException("network down"))
        catalog.sitePublishedResults += Result.success(true)
        val viewModel = HomeViewModel(testAppDependencies(catalog))

        viewModel.onIntent(HomeIntent.Shown)
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.siteStatusCheckFailed)

        viewModel.onIntent(HomeIntent.RetrySiteStatus)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(true, state.sitePublished)
        assertEquals(false, state.siteStatusCheckFailed)
    }

    @Test
    fun shown_fetchReturnsFalse_showsClosedWithoutError() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.sitePublishedResults += Result.success(false)
        val viewModel = HomeViewModel(testAppDependencies(catalog))

        viewModel.onIntent(HomeIntent.Shown)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.sitePublished)
        assertEquals(false, state.siteStatusCheckFailed)
    }

    @Test
    fun shown_fetchHangs_timesOutAsFailure() = runTest(dispatcher) {
        val catalog = ControllableCatalog(hangSitePublished = true)
        val viewModel = HomeViewModel(testAppDependencies(catalog))

        viewModel.onIntent(HomeIntent.Shown)
        advanceTimeBy(16_000L)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertNull(state.sitePublished)
        assertEquals(true, state.siteStatusCheckFailed)
    }

    @Test
    fun startQuiz_recheckFails_showsLoadFailedError() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.sitePublishedResults += Result.success(true)
        catalog.sitePublishedResults += Result.failure(IllegalStateException("network down"))
        val viewModel = HomeViewModel(testAppDependencies(catalog))

        viewModel.onIntent(HomeIntent.Shown)
        advanceUntilIdle()
        viewModel.onIntent(HomeIntent.NicknameChanged("Alice"))
        viewModel.onIntent(HomeIntent.StartQuiz)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertIs<HomeError.LoadFailed>(state.error)
        assertEquals(false, state.isLoading)
        // 障害を「受付前」に見せない
        assertEquals(true, state.sitePublished)
    }

    @Test
    fun shown_whilePublished_keepsOpenUiDuringRecheck() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.sitePublishedResults += Result.success(true)
        val viewModel = HomeViewModel(testAppDependencies(catalog))

        viewModel.onIntent(HomeIntent.Shown)
        advanceUntilIdle()
        assertEquals(true, viewModel.uiState.value.sitePublished)

        catalog.hangNextCalls = 1
        viewModel.onIntent(HomeIntent.Shown)
        runCurrent()
        // 再チェック中も受付オープン UI を落とさない
        assertEquals(true, viewModel.uiState.value.sitePublished)
        assertEquals(false, viewModel.uiState.value.siteStatusCheckFailed)
    }

    @Test
    fun shown_cancelsInFlightRefresh_soStaleTimeoutDoesNotOverwrite() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.hangNextCalls = 1
        val viewModel = HomeViewModel(testAppDependencies(catalog))

        viewModel.onIntent(HomeIntent.Shown)
        // Job1 を開始して withTimeout + hang まで進める（仮想時刻は進めない）
        runCurrent()
        assertNull(viewModel.uiState.value.sitePublished)

        catalog.sitePublishedResults += Result.success(true)
        viewModel.onIntent(HomeIntent.Shown)
        // 遅延タイムアウトを発火させず、今すぐ実行可能な Job2 だけ進める
        runCurrent()
        assertEquals(true, viewModel.uiState.value.sitePublished)
        assertEquals(false, viewModel.uiState.value.siteStatusCheckFailed)

        // 旧 Job のタイムアウト相当の時間が経っても、世代ガードで成功状態を維持する
        advanceTimeBy(16_000L)
        runCurrent()
        assertEquals(true, viewModel.uiState.value.sitePublished)
        assertEquals(false, viewModel.uiState.value.siteStatusCheckFailed)
    }

    private class ControllableCatalog(private val hangSitePublished: Boolean = false) : QuizCatalogRepository {
        val sitePublishedResults = ArrayDeque<Result<Boolean>>()
        /** 次の N 回の getSitePublished を awaitCancellation する（再入 cancel の検証用）。 */
        var hangNextCalls: Int = 0

        override suspend fun getSitePublished(): Boolean {
            if (hangSitePublished || hangNextCalls > 0) {
                if (hangNextCalls > 0) hangNextCalls -= 1
                awaitCancellation()
            }
            return sitePublishedResults.removeFirst().getOrThrow()
        }

        override suspend fun listFolders(): List<QuizFolder> = fail("unused")
        override suspend fun createFolder(name: String, description: String): QuizFolder = fail("unused")
        override suspend fun updateFolder(folder: QuizFolder) = fail("unused")
        override suspend fun deleteFolder(folderId: String) = fail("unused")
        override suspend fun getQuizSet(folderId: String): QuizSet = fail("unused")
        override suspend fun saveQuizSet(quizSet: QuizSet) = fail("unused")
        override suspend fun getActiveFolderId(): String = fail("unused")
        override suspend fun setActiveFolderId(folderId: String) = fail("unused")
        override suspend fun setSitePublished(published: Boolean) = fail("unused")
    }

    private class FixedInstantProvider(private val millis: Long) : InstantProvider {
        override fun nowEpochMillis(): Long = millis
    }

    private fun unusedRanking(): RankingRepository = object : RankingRepository {
        override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = emptyList()
        override suspend fun submitScore(
            result: QuizResult,
            completedAtEpochMillis: Long,
            folderId: String,
            entryId: String,
        ) = Unit

        override suspend fun deleteEntry(folderId: String, entryId: String) = Unit
        override suspend fun clearTodayRankings(folderId: String) = Unit
    }

    private fun unusedStaffRepo(): StaffAuthRepository = object : StaffAuthRepository {
        override suspend fun signIn(email: String, password: String): Result<StaffSession> = fail("unused")
    }

    private fun unusedSessionStore(): StaffAuthSessionStore = object : StaffAuthSessionStore {
        override var currentSession: StaffSession? = null
    }

    private fun testAppDependencies(catalog: QuizCatalogRepository): AppDependencies {
        val ranking = unusedRanking()
        val staffRepo = unusedStaffRepo()
        val staffStore = unusedSessionStore()
        val signIn = SignInStaffUseCase(staffRepo, staffStore)
        val quizEngine = QuizEngine()
        val sessionHolder = QuizSessionHolder()
        val instantProvider = FixedInstantProvider(1_700_000_000_000L)
        return AppDependencies(
            instantProvider = instantProvider,
            quizCatalogRepository = catalog,
            quizEngine = quizEngine,
            sessionHolder = sessionHolder,
            quizPlayUseCase = QuizPlayUseCase(
                quizEngine = quizEngine,
                sessionStore = sessionHolder,
                submitScoreUseCase = SubmitScoreUseCase(ranking),
                instantProvider = instantProvider,
            ),
            getTodayRankingsUseCase = GetTodayRankingsUseCase(ranking),
            deleteRankingEntryUseCase = DeleteRankingEntryUseCase(ranking),
            clearTodayRankingsUseCase = ClearTodayRankingsUseCase(ranking),
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
