package com.droidkaigi.quiz.feature.staff

import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
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

@OptIn(ExperimentalCoroutinesApi::class)
class StaffShellViewModelFolderEditTest {
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
    fun updateFolder_persistsTrimmedNameAndDescription() = runTest {
        val catalog = RecordingCatalogRepository()
        val viewModel = StaffShellViewModel(staffTestAppDependencies(catalog, NoopRankingRepository))

        viewModel.onIntent(StaffShellIntent.ShowEditFolderDialog("day1"))
        assertEquals("Day 1", viewModel.uiState.value.editingFolder?.name)

        viewModel.onIntent(StaffShellIntent.UpdateFolder("day1", "  Day 1 · Easy  ", "  初級向け  "))

        assertEquals(
            listOf(QuizFolder(id = "day1", name = "Day 1 · Easy", description = "初級向け", sortOrder = 0)),
            catalog.updates,
        )
        val state = viewModel.uiState.value
        assertNull(state.editingFolderId)
        assertEquals("Day 1 · Easy", state.folders.first { it.id == "day1" }.name)
    }

    @Test
    fun updateFolder_withBlankName_keepsDialogOpenAndSkipsWrite() = runTest {
        val catalog = RecordingCatalogRepository()
        val viewModel = StaffShellViewModel(staffTestAppDependencies(catalog, NoopRankingRepository))

        viewModel.onIntent(StaffShellIntent.ShowEditFolderDialog("day1"))
        viewModel.onIntent(StaffShellIntent.UpdateFolder("day1", "   ", "初級向け"))

        assertEquals(emptyList(), catalog.updates)
        assertEquals("day1", viewModel.uiState.value.editingFolderId)
    }

    @Test
    fun updateFolder_failure_surfacesErrorAndKeepsDialogOpen() = runTest {
        val catalog = RecordingCatalogRepository(updateError = IllegalStateException("network down"))
        val viewModel = StaffShellViewModel(staffTestAppDependencies(catalog, NoopRankingRepository))

        viewModel.onIntent(StaffShellIntent.ShowEditFolderDialog("day1"))
        viewModel.onIntent(StaffShellIntent.UpdateFolder("day1", "Day 1 · Easy", ""))

        val state = viewModel.uiState.value
        assertEquals("network down", state.errorMessage)
        assertEquals("day1", state.editingFolderId)
    }

    private class RecordingCatalogRepository(private val updateError: Throwable? = null) : QuizCatalogRepository {
        val updates = mutableListOf<QuizFolder>()
        private val folders = mutableListOf(
            QuizFolder(id = "day1", name = "Day 1", description = "", sortOrder = 0),
            QuizFolder(id = "day2", name = "Day 2", description = "", sortOrder = 1),
        )

        override suspend fun listFolders(): List<QuizFolder> = folders.toList()

        override suspend fun createFolder(name: String, description: String): QuizFolder = error("unused")

        override suspend fun updateFolder(folder: QuizFolder) {
            updateError?.let { throw it }
            updates += folder
            val index = folders.indexOfFirst { it.id == folder.id }
            if (index >= 0) folders[index] = folder
        }

        override suspend fun deleteFolder(folderId: String) = error("unused")

        override suspend fun getQuizSet(folderId: String): QuizSet = error("unused")

        override suspend fun saveQuizSet(quizSet: QuizSet) = error("unused")

        override suspend fun getActiveFolderId(): String = "day1"

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
