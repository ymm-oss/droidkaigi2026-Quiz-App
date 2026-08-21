package jp.co.yumemi.quiz.droidkaigi.feature.ranking

import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.QuizSessionHolder
import jp.co.yumemi.quiz.droidkaigi.core.data.StaffAuthHolder
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
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.RestoreStaffAuthSessionUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuizPlayUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SaveQuizSetUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SetActiveQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignOutStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SubmitScoreUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.UpdateQuizFolderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class RankingViewModelTest {
    private val testDispatcher = UnconfinedTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoad_success_showsEntries() = runTest {
        val entries = listOf(RankingEntry("Alice", 100, 1_700_000_000_000))
        val viewModel = RankingViewModel(
            rankingTestDeps(
                rankings = { entries },
            ),
        )

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertEquals(entries, state.entries)
    }

    @Test
    fun initialLoad_failure_showsErrorWithoutEntries() = runTest {
        val viewModel = RankingViewModel(
            rankingTestDeps(
                rankings = { error("network down") },
            ),
        )

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertTrue(state.entries.isEmpty())
        val error = assertIs<RankingError.LoadFailed>(state.error)
        assertEquals("network down", error.detail)
    }

    @Test
    fun retryAfterInitialFailure_success_clearsError() = runTest {
        var failOnce = true
        val entries = listOf(RankingEntry("Bob", 200, 1_700_000_000_100))
        val viewModel = RankingViewModel(
            rankingTestDeps(
                rankings = {
                    if (failOnce) {
                        failOnce = false
                        error("temporary")
                    } else {
                        entries
                    }
                },
            ),
        )

        assertIs<RankingError.LoadFailed>(viewModel.uiState.value.error)

        viewModel.onIntent(RankingIntent.Refresh)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertNull(state.error)
        assertEquals(entries, state.entries)
    }

    @Test
    fun refreshAfterSuccess_failure_keepsEntriesAndShowsError() = runTest {
        var call = 0
        val entries = listOf(RankingEntry("Carol", 300, 1_700_000_000_200))
        val viewModel = RankingViewModel(
            rankingTestDeps(
                rankings = {
                    call += 1
                    if (call == 1) {
                        entries
                    } else {
                        error("refresh failed")
                    }
                },
            ),
        )

        assertEquals(entries, viewModel.uiState.value.entries)
        assertNull(viewModel.uiState.value.error)

        viewModel.onIntent(RankingIntent.Refresh)

        val state = viewModel.uiState.value
        assertEquals(false, state.isLoading)
        assertEquals(entries, state.entries)
        val error = assertIs<RankingError.LoadFailed>(state.error)
        assertEquals("refresh failed", error.detail)
    }
}

private fun rankingTestDeps(
    rankings: suspend (String) -> List<RankingEntry>,
    sessionHolder: QuizSessionHolder = QuizSessionHolder().apply {
        playbackFolderId = "folder-1"
        highlightNickname = "Alice"
    },
): AppDependencies {
    val rankingRepository = object : RankingRepository {
        override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = rankings(folderId)

        override suspend fun submitScore(
            result: QuizResult,
            completedAtEpochMillis: Long,
            folderId: String,
            entryId: String,
        ) = Unit

        override suspend fun deleteEntry(folderId: String, entryId: String) = Unit

        override suspend fun clearTodayRankings(folderId: String) = Unit
    }
    val catalogRepository = object : QuizCatalogRepository {
        override suspend fun listFolders(): List<QuizFolder> = error("unused")

        override suspend fun createFolder(name: String, description: String): QuizFolder = error("unused")

        override suspend fun updateFolder(folder: QuizFolder) = error("unused")

        override suspend fun deleteFolder(folderId: String) = error("unused")

        override suspend fun getQuizSet(folderId: String): QuizSet = error("unused")

        override suspend fun saveQuizSet(quizSet: QuizSet) = error("unused")

        override suspend fun getActiveFolderId(): String = "active-folder"

        override suspend fun setActiveFolderId(folderId: String) = error("unused")

        override suspend fun getSitePublished(): Boolean = true

        override suspend fun setSitePublished(published: Boolean) = error("unused")
    }
    val instantProvider = object : InstantProvider {
        override fun nowEpochMillis(): Long = 0L
    }
    val staffAuthRepository = object : StaffAuthRepository {
        override suspend fun signIn(email: String, password: String): Result<StaffSession> =
            Result.failure(IllegalStateException("unused"))
    }
    val staffAuthHolder = StaffAuthHolder()
    val signInStaffUseCase = SignInStaffUseCase(staffAuthRepository, staffAuthHolder)

    val quizEngine = QuizEngine()
    val submitScoreUseCase = SubmitScoreUseCase(rankingRepository)
    return AppDependencies(
        instantProvider = instantProvider,
        quizCatalogRepository = catalogRepository,
        quizEngine = quizEngine,
        sessionHolder = sessionHolder,
        quizPlayUseCase = QuizPlayUseCase(
            quizEngine = quizEngine,
            sessionStore = sessionHolder,
            submitScoreUseCase = submitScoreUseCase,
            instantProvider = instantProvider,
        ),
        getTodayRankingsUseCase = GetTodayRankingsUseCase(rankingRepository),
        deleteRankingEntryUseCase = DeleteRankingEntryUseCase(rankingRepository),
        clearTodayRankingsUseCase = ClearTodayRankingsUseCase(rankingRepository),
        listQuizFoldersUseCase = ListQuizFoldersUseCase(catalogRepository),
        createQuizFolderUseCase = CreateQuizFolderUseCase(catalogRepository),
        updateQuizFolderUseCase = UpdateQuizFolderUseCase(catalogRepository),
        deleteQuizFolderUseCase = DeleteQuizFolderUseCase(catalogRepository),
        getQuizSetForFolderUseCase = GetQuizSetForFolderUseCase(catalogRepository),
        saveQuizSetUseCase = SaveQuizSetUseCase(catalogRepository),
        getActiveQuizFolderIdUseCase = GetActiveQuizFolderIdUseCase(catalogRepository),
        setActiveQuizFolderUseCase = SetActiveQuizFolderUseCase(catalogRepository),
        signInStaffUseCase = signInStaffUseCase,
        quickSignInStaffUseCase = QuickSignInStaffUseCase(staffAuthRepository, signInStaffUseCase),
        restoreStaffAuthSessionUseCase = RestoreStaffAuthSessionUseCase(staffAuthRepository, staffAuthHolder),
        getStaffAuthStateUseCase = GetStaffAuthStateUseCase(staffAuthHolder),
        signOutStaffUseCase = SignOutStaffUseCase(staffAuthHolder, staffAuthRepository),
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
