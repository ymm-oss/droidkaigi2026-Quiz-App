package com.droidkaigi.quiz.feature.quiz.home

import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.data.QuizSessionHolder
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
import com.droidkaigi.quiz.core.domain.usecase.StaffAuthSessionStore
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import com.droidkaigi.quiz.core.domain.usecase.UpdateQuizFolderUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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

    private class ControllableCatalog(private val hangSitePublished: Boolean = false) : QuizCatalogRepository {
        val sitePublishedResults = ArrayDeque<Result<Boolean>>()

        override suspend fun getSitePublished(): Boolean {
            if (hangSitePublished) awaitCancellation()
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
            getStaffAuthStateUseCase = GetStaffAuthStateUseCase(staffStore),
            signOutStaffUseCase = SignOutStaffUseCase(staffStore),
        )
    }
}
