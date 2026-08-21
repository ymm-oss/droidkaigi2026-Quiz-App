package jp.co.yumemi.quiz.droidkaigi.feature.staff

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class StaffShellViewModelFolderDeleteTest {
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
    fun deleteFolder_removesFolderAndReselectsRemaining() = runTest {
        val catalog = RecordingCatalogRepository()
        val viewModel = StaffShellViewModel(staffTestAppDependencies(catalog, NoopRankingRepository))

        viewModel.onIntent(StaffShellIntent.SelectFolder("day1"))
        viewModel.onIntent(StaffShellIntent.RequestDeleteFolder("day1"))
        assertEquals("day1", viewModel.uiState.value.deletingFolderId)

        viewModel.onIntent(StaffShellIntent.ConfirmDeleteFolder)

        assertEquals(listOf("day1"), catalog.deletedIds)
        val state = viewModel.uiState.value
        assertNull(state.deletingFolderId)
        assertTrue(state.folders.none { it.id == "day1" })
        assertEquals("day2", state.selectedFolderId)
        assertEquals("day2", state.activeFolderId)
    }

    @Test
    fun deleteFolder_lastFolder_isBlocked() = runTest {
        val catalog = RecordingCatalogRepository(initialFolders = listOf(
            QuizFolder(id = "only", name = "Only", description = "", sortOrder = 0),
        ))
        val viewModel = StaffShellViewModel(staffTestAppDependencies(catalog, NoopRankingRepository))

        viewModel.onIntent(StaffShellIntent.RequestDeleteFolder("only"))
        viewModel.onIntent(StaffShellIntent.ConfirmDeleteFolder)

        assertTrue(catalog.deletedIds.isEmpty())
        val state = viewModel.uiState.value
        assertNull(state.deletingFolderId)
        assertEquals("最後のフォルダは削除できません", state.errorMessage)
        assertEquals(1, state.folders.size)
    }

    @Test
    fun deleteFolder_failure_keepsDialogOpenAndSurfacesError() = runTest {
        val catalog = RecordingCatalogRepository(deleteError = IllegalStateException("network down"))
        val viewModel = StaffShellViewModel(staffTestAppDependencies(catalog, NoopRankingRepository))

        viewModel.onIntent(StaffShellIntent.RequestDeleteFolder("day1"))
        viewModel.onIntent(StaffShellIntent.ConfirmDeleteFolder)

        val state = viewModel.uiState.value
        assertEquals("day1", state.deletingFolderId)
        assertEquals("network down", state.errorMessage)
    }

    @Test
    fun deleteFolder_inFlight_blocksAdditionalDeleteUntilLocalListUpdates() = runTest {
        val gate = CompletableDeferred<Unit>()
        val catalog = RecordingCatalogRepository(deleteGate = gate)
        val viewModel = StaffShellViewModel(staffTestAppDependencies(catalog, NoopRankingRepository))

        viewModel.onIntent(StaffShellIntent.RequestDeleteFolder("day1"))
        viewModel.onIntent(StaffShellIntent.ConfirmDeleteFolder)

        // While the first delete is still awaiting the backend, reject another delete path.
        viewModel.onIntent(StaffShellIntent.DismissDeleteFolderDialog)
        viewModel.onIntent(StaffShellIntent.RequestDeleteFolder("day2"))
        viewModel.onIntent(StaffShellIntent.ConfirmDeleteFolder)
        assertTrue(catalog.deletedIds.isEmpty())
        assertNull(viewModel.uiState.value.deletingFolderId)
        assertEquals(2, viewModel.uiState.value.folders.size)

        gate.complete(Unit)

        assertEquals(listOf("day1"), catalog.deletedIds)
        val state = viewModel.uiState.value
        assertTrue(state.folders.none { it.id == "day1" })
        assertTrue(state.folders.any { it.id == "day2" })
        assertEquals(1, state.folders.size)
    }

    private class RecordingCatalogRepository(
        initialFolders: List<QuizFolder> = listOf(
            QuizFolder(id = "day1", name = "Day 1", description = "", sortOrder = 0),
            QuizFolder(id = "day2", name = "Day 2", description = "", sortOrder = 1),
        ),
        private val deleteError: Throwable? = null,
        private val deleteGate: CompletableDeferred<Unit>? = null,
    ) : QuizCatalogRepository {
        val deletedIds = mutableListOf<String>()
        private val folders = initialFolders.toMutableList()
        private var activeFolderId: String = folders.first().id

        override suspend fun listFolders(): List<QuizFolder> = folders.toList()

        override suspend fun createFolder(name: String, description: String): QuizFolder = error("unused")

        override suspend fun updateFolder(folder: QuizFolder) = error("unused")

        override suspend fun deleteFolder(folderId: String) {
            deleteGate?.await()
            deleteError?.let { throw it }
            deletedIds += folderId
            folders.removeAll { it.id == folderId }
            if (activeFolderId == folderId) {
                activeFolderId = folders.minByOrNull { it.sortOrder }?.id.orEmpty()
            }
        }

        override suspend fun getQuizSet(folderId: String): QuizSet = error("unused")

        override suspend fun saveQuizSet(quizSet: QuizSet) = error("unused")

        override suspend fun getActiveFolderId(): String = activeFolderId

        override suspend fun setActiveFolderId(folderId: String) = error("unused")

        override suspend fun getSitePublished(): Boolean = false

        override suspend fun setSitePublished(published: Boolean) = Unit
    }

    private object NoopRankingRepository : RankingRepository {
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
}
