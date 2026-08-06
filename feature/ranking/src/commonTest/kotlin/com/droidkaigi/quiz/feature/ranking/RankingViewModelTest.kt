package com.droidkaigi.quiz.feature.ranking

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
import com.droidkaigi.quiz.core.domain.usecase.QuizPlayUseCase
import com.droidkaigi.quiz.core.domain.usecase.SaveQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetActiveQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignOutStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import com.droidkaigi.quiz.core.domain.usecase.UpdateQuizFolderUseCase
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
        getStaffAuthStateUseCase = GetStaffAuthStateUseCase(staffAuthHolder),
        signOutStaffUseCase = SignOutStaffUseCase(staffAuthHolder),
    )
}
