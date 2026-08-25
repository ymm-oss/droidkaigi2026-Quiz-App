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
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuickSignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.RestoreStaffAuthSessionUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuizPlayUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SaveQuizSetUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SetActiveQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignOutStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.StaffAuthSessionStore
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SubmitScoreUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.UpdateQuizFolderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

/**
 * 構成変更後の再 composition による `syncFromSession()` が、同一セッション中の
 * 未提出の選択・フィードバック状態を初期化しないことを検証する。
 * https://github.com/ymm-oss/droidkaigi2026-Quiz-App/pull/78 のレビュー指摘。
 */
@OptIn(ExperimentalCoroutinesApi::class)
class QuizViewModelSyncFromSessionTest {
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
    fun syncFromSession_sameSession_keepsUnsubmittedSelection() = runTest(dispatcher) {
        val sessionHolder = QuizSessionHolder()
        val deps = testAppDependencies(sessionHolder)
        beginSession(deps, sessionHolder, startedAt = 1_700_000_000_000L)
        val viewModel = QuizViewModel(deps)
        viewModel.onIntent(QuizIntent.SelectSingle("a"))

        // 構成変更後に LaunchedEffect が再実行されたのと同じ経路
        viewModel.syncFromSession()

        assertEquals("a", viewModel.uiState.value.selectedSingleId)
        assertEquals(true, viewModel.uiState.value.canSubmit)
    }

    @Test
    fun syncFromSession_sameSession_keepsFeedbackOverlay() = runTest(dispatcher) {
        val sessionHolder = QuizSessionHolder()
        val deps = testAppDependencies(sessionHolder)
        beginSession(deps, sessionHolder, startedAt = 1_700_000_000_000L, questionCount = 2)
        val viewModel = QuizViewModel(deps)
        viewModel.onIntent(QuizIntent.SelectSingle("a"))
        viewModel.onIntent(QuizIntent.SubmitAnswer)
        assertEquals(true, viewModel.uiState.value.showFeedback)

        viewModel.syncFromSession()

        assertEquals(true, viewModel.uiState.value.showFeedback)
        assertEquals(true, viewModel.uiState.value.lastAnswerCorrect)
    }

    @Test
    fun syncFromSession_newSession_resyncsToFreshState() = runTest(dispatcher) {
        val sessionHolder = QuizSessionHolder()
        val deps = testAppDependencies(sessionHolder)
        beginSession(deps, sessionHolder, startedAt = 1_700_000_000_000L)
        val viewModel = QuizViewModel(deps)
        viewModel.onIntent(QuizIntent.SelectSingle("a"))

        beginSession(deps, sessionHolder, startedAt = 1_700_000_060_000L)
        viewModel.syncFromSession()

        assertNull(viewModel.uiState.value.selectedSingleId)
        assertEquals(false, viewModel.uiState.value.canSubmit)
    }

    private fun beginSession(
        deps: AppDependencies,
        sessionHolder: QuizSessionHolder,
        startedAt: Long,
        questionCount: Int = 1,
    ) {
        val questions = (1..questionCount).map { index ->
            SingleChoice(
                id = "q$index",
                prompt = "Q$index",
                options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B")),
                correctId = "a",
            )
        }
        sessionHolder.beginSession(
            deps.quizEngine.startSession(
                folderId = "folder",
                quizSet = QuizSet("folder", "Demo", questions),
                nickname = "Alice",
                startedAtEpochMillis = startedAt,
            ),
        )
    }

    private class FixedInstantProvider(private val millis: Long) : InstantProvider {
        override fun nowEpochMillis(): Long = millis
    }

    private fun noopRanking(): RankingRepository = object : RankingRepository {
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
    }

    private fun unusedStaffRepo(): StaffAuthRepository = object : StaffAuthRepository {
        override suspend fun signIn(email: String, password: String): Result<StaffSession> =
            fail("unused")
    }

    private fun unusedSessionStore(): StaffAuthSessionStore = object : StaffAuthSessionStore {
        override var currentSession: StaffSession? = null
    }

    private fun testAppDependencies(sessionHolder: QuizSessionHolder): AppDependencies {
        val catalog = unusedCatalog()
        val ranking = noopRanking()
        val staffRepo = unusedStaffRepo()
        val staffStore = unusedSessionStore()
        val signIn = SignInStaffUseCase(staffRepo, staffStore)
        val quizEngine = QuizEngine()
        val instantProvider = FixedInstantProvider(1_700_000_000_000L)
        return AppDependencies(
            instantProvider = instantProvider,
            quizCatalogRepository = catalog,
            quizEngine = quizEngine,
            sessionHolder = sessionHolder,
            siteStatusHolder = SiteStatusHolder(),
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
