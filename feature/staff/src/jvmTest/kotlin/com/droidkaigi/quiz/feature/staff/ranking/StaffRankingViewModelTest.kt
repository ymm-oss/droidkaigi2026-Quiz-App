package com.droidkaigi.quiz.feature.staff.ranking

import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.data.QuizSessionHolder
import com.droidkaigi.quiz.core.data.StaffAuthHolder
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.model.RankingEntry
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
import com.droidkaigi.quiz.core.domain.usecase.RestoreStaffAuthSessionUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuizPlayUseCase
import com.droidkaigi.quiz.core.domain.usecase.SaveQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetActiveQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignOutStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import com.droidkaigi.quiz.core.domain.usecase.UpdateQuizFolderUseCase
import kotlinx.coroutines.CompletableDeferred
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
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StaffRankingViewModelTest {
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
    fun refreshFailure_keepsPreviouslyLoadedEntries() = runTest {
        val entries = listOf(RankingEntry("Alice", 100, 1_700_000_000_000, id = "entry-1"))
        var shouldFail = false
        val viewModel = StaffRankingViewModel(
            folderId = "folder-1",
            deps = staffRankingTestDeps(
                rankings = {
                    if (shouldFail) error("network down") else entries
                },
            ),
        )

        assertEquals(entries, viewModel.uiState.value.entries)

        shouldFail = true
        viewModel.onIntent(StaffRankingIntent.Refresh)

        val state = viewModel.uiState.value
        assertEquals(entries, state.entries)
        assertEquals("network down", state.loadError)
        assertFalse(state.isLoading)
    }

    @Test
    fun deleteSuccess_reloadFailure_optimisticallyRemovesEntryAndResetsMutating() = runTest {
        val entries = listOf(RankingEntry("Alice", 100, 1_700_000_000_000, id = "entry-1"))
        var failReloadAfterDelete = false
        val viewModel = StaffRankingViewModel(
            folderId = "folder-1",
            deps = staffRankingTestDeps(
                rankings = {
                    if (failReloadAfterDelete) error("reload failed") else entries
                },
                onDelete = { failReloadAfterDelete = true },
            ),
        )

        viewModel.onIntent(StaffRankingIntent.DeleteEntry("entry-1"))

        val state = viewModel.uiState.value
        assertTrue(state.entries.isEmpty())
        assertEquals("操作は完了しましたが、一覧の更新に失敗しました", state.mutationError)
        assertFalse(state.isMutating)
    }

    @Test
    fun deleteFailure_keepsEntriesAndResetsMutating() = runTest {
        val entries = listOf(RankingEntry("Alice", 100, 1_700_000_000_000, id = "entry-1"))
        val viewModel = StaffRankingViewModel(
            folderId = "folder-1",
            deps = staffRankingTestDeps(
                rankings = { entries },
                onDelete = { error("delete failed") },
            ),
        )

        viewModel.onIntent(StaffRankingIntent.DeleteEntry("entry-1"))

        val state = viewModel.uiState.value
        assertEquals(entries, state.entries)
        assertEquals("delete failed", state.mutationError)
        assertFalse(state.isMutating)
    }

    @Test
    fun staleRefresh_doesNotOverwriteAfterDelete() = runTest {
        val original = listOf(RankingEntry("Alice", 100, 1_700_000_000_000, id = "entry-1"))
        val refreshed = listOf(
            RankingEntry("Alice", 100, 1_700_000_000_000, id = "entry-1"),
            RankingEntry("Bob", 90, 1_700_000_000_100, id = "entry-2"),
        )
        val refreshGate = CompletableDeferred<Unit>()
        var refreshCount = 0
        val viewModel = StaffRankingViewModel(
            folderId = "folder-1",
            deps = staffRankingTestDeps(
                rankings = {
                    refreshCount += 1
                    when (refreshCount) {
                        1 -> original
                        2 -> {
                            refreshGate.await()
                            refreshed
                        }
                        else -> emptyList()
                    }
                },
            ),
        )
        assertEquals(original, viewModel.uiState.value.entries)

        viewModel.onIntent(StaffRankingIntent.Refresh)
        viewModel.onIntent(StaffRankingIntent.DeleteEntry("entry-1"))
        refreshGate.complete(Unit)

        assertTrue(viewModel.uiState.value.entries.isEmpty())
        assertNull(viewModel.uiState.value.mutationError)
    }
}

private fun staffRankingTestDeps(
    rankings: suspend (String) -> List<RankingEntry>,
    onDelete: suspend (String) -> Unit = {},
    onClear: suspend () -> Unit = {},
): AppDependencies {
    val rankingRepository = object : RankingRepository {
        override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = rankings(folderId)

        override suspend fun submitScore(
            result: QuizResult,
            completedAtEpochMillis: Long,
            folderId: String,
            entryId: String,
        ) = Unit

        override suspend fun deleteEntry(folderId: String, entryId: String) {
            onDelete(entryId)
        }

        override suspend fun clearTodayRankings(folderId: String) {
            onClear()
        }
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

        override suspend fun setSitePublished(published: Boolean) = Unit
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
    val sessionHolder = QuizSessionHolder()
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
    )
}
