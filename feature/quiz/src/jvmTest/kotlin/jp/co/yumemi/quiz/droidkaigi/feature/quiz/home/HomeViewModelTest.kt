package jp.co.yumemi.quiz.droidkaigi.feature.quiz.home

import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.QuizSessionHolder
import jp.co.yumemi.quiz.droidkaigi.core.data.SiteStatusHolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.AppConfigStatus
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
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ObserveTodayRankingsUseCase
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.fail

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
    fun holderUnpublished_showsClosedWithoutShown() = runTest(dispatcher) {
        val holder = SiteStatusHolder()
        val viewModel = HomeViewModel(testAppDependencies(holder = holder))

        holder.applyStatus(AppConfigStatus(sitePublished = false, activeFolderId = "day1"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(false, state.sitePublished)
        assertEquals(false, state.siteStatusCheckFailed)
        assertEquals(false, state.isSiteOpen)
    }

    @Test
    fun holderPublished_opensIntake() = runTest(dispatcher) {
        val holder = SiteStatusHolder()
        val viewModel = HomeViewModel(testAppDependencies(holder = holder))

        holder.applyStatus(AppConfigStatus(sitePublished = true, activeFolderId = "day1"))
        advanceUntilIdle()

        assertEquals(true, viewModel.uiState.value.sitePublished)
        assertEquals(true, viewModel.uiState.value.isSiteOpen)
    }

    @Test
    fun liveUnpublish_closesIntake() = runTest(dispatcher) {
        val holder = SiteStatusHolder()
        val viewModel = HomeViewModel(testAppDependencies(holder = holder))
        holder.applyStatus(AppConfigStatus(sitePublished = true, activeFolderId = "day1"))
        advanceUntilIdle()

        holder.applyStatus(AppConfigStatus(sitePublished = false, activeFolderId = "day1"))
        advanceUntilIdle()

        assertEquals(false, viewModel.uiState.value.sitePublished)
        assertEquals(false, viewModel.uiState.value.isSiteOpen)
    }

    @Test
    fun observeFailedBeforeFirstSnapshot_showsRetry() = runTest(dispatcher) {
        val holder = SiteStatusHolder()
        val viewModel = HomeViewModel(testAppDependencies(holder = holder))
        holder.markObserveFailed()
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.sitePublished)
        assertEquals(true, viewModel.uiState.value.siteStatusCheckFailed)
    }

    @Test
    fun retry_incrementsHolderRetryToken() = runTest(dispatcher) {
        val holder = SiteStatusHolder()
        holder.markObserveFailed()
        val viewModel = HomeViewModel(testAppDependencies(holder = holder))
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.RetrySiteStatus)
        advanceUntilIdle()

        assertEquals(1, holder.retryToken.value)
        assertEquals(false, holder.observeFailed.value)
    }

    @Test
    fun published_loadsPublishedFoldersAndSelectsSingle() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.publishedFolders = listOf(QuizFolder(id = "easy", name = "一般向け", sortOrder = 0))
        val holder = SiteStatusHolder()
        val viewModel = HomeViewModel(testAppDependencies(catalog, holder))

        holder.applyStatus(AppConfigStatus(sitePublished = true, activeFolderId = "easy"))
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertEquals(listOf("easy"), state.publishedFolders?.map { it.id })
        assertEquals("easy", state.selectedFolderId)
    }

    @Test
    fun publishedFolderIdsChange_reloadsPicker() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.publishedFolders = listOf(QuizFolder(id = "easy", name = "一般向け", sortOrder = 0))
        val holder = SiteStatusHolder()
        val viewModel = HomeViewModel(testAppDependencies(catalog, holder))
        holder.applyStatus(AppConfigStatus(sitePublished = true, activeFolderId = "easy"))
        advanceUntilIdle()
        assertEquals(listOf("easy"), viewModel.uiState.value.publishedFolders?.map { it.id })

        catalog.publishedFolders = listOf(
            QuizFolder(id = "easy", name = "一般向け", sortOrder = 0),
            QuizFolder(id = "hard", name = "高難易度", sortOrder = 1),
        )
        holder.applyStatus(
            AppConfigStatus(
                sitePublished = true,
                activeFolderId = "easy",
                publishedFolderIds = listOf("easy", "hard"),
            ),
        )
        advanceUntilIdle()

        assertEquals(listOf("easy", "hard"), viewModel.uiState.value.publishedFolders?.map { it.id })
    }

    @Test
    fun publishedFoldersLoadFailure_showsLoadFailed() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.listError = IllegalStateException("config down")
        val holder = SiteStatusHolder()
        val viewModel = HomeViewModel(testAppDependencies(catalog, holder))
        holder.applyStatus(AppConfigStatus(sitePublished = true, activeFolderId = "easy"))
        advanceUntilIdle()

        assertIs<HomeError.LoadFailed>(viewModel.uiState.value.error)
        assertNull(viewModel.uiState.value.publishedFolders)
    }

    @Test
    fun startQuiz_twoFoldersWithoutSelection_showsSelectError() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.publishedFolders = listOf(
            QuizFolder(id = "easy", name = "一般向け", sortOrder = 0),
            QuizFolder(id = "hard", name = "高難易度", sortOrder = 1),
        )
        val holder = SiteStatusHolder()
        val viewModel = HomeViewModel(testAppDependencies(catalog, holder))
        holder.applyStatus(
            AppConfigStatus(
                sitePublished = true,
                activeFolderId = "easy",
                publishedFolderIds = listOf("easy", "hard"),
            ),
        )
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.NicknameChanged("Alice"))
        viewModel.onIntent(HomeIntent.StartQuiz)
        advanceUntilIdle()

        assertIs<HomeError.NoFolderSelected>(viewModel.uiState.value.error)
        assertEquals(false, viewModel.uiState.value.isLoading)
    }

    @Test
    fun startQuiz_twoFoldersWithSelection_startsSelectedFolder() = runTest(dispatcher) {
        val catalog = ControllableCatalog()
        catalog.publishedFolders = listOf(
            QuizFolder(id = "easy", name = "一般向け", sortOrder = 0),
            QuizFolder(id = "hard", name = "高難易度", description = "上級者向け", sortOrder = 1),
        )
        catalog.quizSets["hard"] = QuizSet(id = "hard", title = "高難易度", questions = emptyList())
        val holder = SiteStatusHolder()
        val viewModel = HomeViewModel(testAppDependencies(catalog, holder))
        holder.applyStatus(
            AppConfigStatus(
                sitePublished = true,
                activeFolderId = "easy",
                publishedFolderIds = listOf("easy", "hard"),
            ),
        )
        advanceUntilIdle()

        viewModel.onIntent(HomeIntent.SelectPublishedFolder("hard"))
        viewModel.onIntent(HomeIntent.NicknameChanged("Alice"))
        viewModel.onIntent(HomeIntent.StartQuiz)
        advanceUntilIdle()

        assertNull(viewModel.uiState.value.error)
        assertEquals("hard", catalog.lastQuizSetFolderId)
    }

    private class ControllableCatalog : QuizCatalogRepository {
        var publishedFolders: List<QuizFolder> = emptyList()
        val quizSets = mutableMapOf<String, QuizSet>()
        var lastQuizSetFolderId: String? = null
        var listError: Throwable? = null

        override suspend fun getSitePublished(): Boolean = fail("unused")
        override suspend fun listFolders(): List<QuizFolder> {
            listError?.let { throw it }
            return publishedFolders
        }
        override suspend fun createFolder(name: String, description: String): QuizFolder = fail("unused")
        override suspend fun updateFolder(folder: QuizFolder) = fail("unused")
        override suspend fun deleteFolder(folderId: String) = fail("unused")
        override suspend fun getQuizSet(folderId: String): QuizSet {
            lastQuizSetFolderId = folderId
            return quizSets[folderId] ?: fail("unused")
        }
        override suspend fun saveQuizSet(quizSet: QuizSet) = fail("unused")
        override suspend fun getActiveFolderId(): String = publishedFolders.firstOrNull()?.id.orEmpty()
        override suspend fun setActiveFolderId(folderId: String) = fail("unused")
        override suspend fun getPublishedFolderIds(): List<String> {
            listError?.let { throw it }
            return publishedFolders.map { it.id }
        }
        override suspend fun setSitePublished(published: Boolean) = fail("unused")
        override fun observeAppConfig(): Flow<AppConfigStatus> = emptyFlow()
    }

    private class UnusedCatalog : QuizCatalogRepository {
        override suspend fun getSitePublished(): Boolean = fail("unused")
        override suspend fun listFolders(): List<QuizFolder> = emptyList()
        override suspend fun createFolder(name: String, description: String): QuizFolder = fail("unused")
        override suspend fun updateFolder(folder: QuizFolder) = fail("unused")
        override suspend fun deleteFolder(folderId: String) = fail("unused")
        override suspend fun getQuizSet(folderId: String): QuizSet = fail("unused")
        override suspend fun saveQuizSet(quizSet: QuizSet) = fail("unused")
        override suspend fun getActiveFolderId(): String = fail("unused")
        override suspend fun setActiveFolderId(folderId: String) = fail("unused")
        override suspend fun getPublishedFolderIds(): List<String> = emptyList()
        override suspend fun setSitePublished(published: Boolean) = fail("unused")
        override fun observeAppConfig(): Flow<AppConfigStatus> = emptyFlow()
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

    private fun testAppDependencies(
        catalog: QuizCatalogRepository = UnusedCatalog(),
        holder: SiteStatusHolder = SiteStatusHolder(),
    ): AppDependencies {
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
            siteStatusHolder = holder,
            quizPlayUseCase = QuizPlayUseCase(
                quizEngine = quizEngine,
                sessionStore = sessionHolder,
                submitScoreUseCase = SubmitScoreUseCase(ranking),
                instantProvider = instantProvider,
            ),
            getTodayRankingsUseCase = GetTodayRankingsUseCase(ranking),
            observeTodayRankingsUseCase = ObserveTodayRankingsUseCase(ranking),
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
