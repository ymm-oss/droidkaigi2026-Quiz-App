package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

/**
 * Process-wide in-memory store for staff-managed folders, quiz sets, and per-folder rankings.
 */
class InMemoryQuizCatalog {
    private val mutex = Mutex()
    private val folders = mutableListOf<QuizFolder>()
    private val quizSets = mutableMapOf<String, QuizSet>()
    private val rankingsByFolder = mutableMapOf<String, MutableList<RankingEntry>>()
    private var activeFolderId: String = ""

    suspend fun <T> withLock(block: suspend InMemoryQuizCatalog.() -> T): T = mutex.withLock { block() }

    fun listFolders(): List<QuizFolder> = folders.sortedBy { it.sortOrder }

    fun createFolder(name: String, description: String): QuizFolder {
        val id = newFolderDocumentId()
        val folder = QuizFolder(
            id = id,
            name = name.trim(),
            description = description.trim(),
            sortOrder = folders.size,
        )
        folders += folder
        quizSets[id] = QuizSet(id = id, title = name.trim(), questions = emptyList())
        rankingsByFolder[id] = mutableListOf()
        if (activeFolderId.isEmpty()) activeFolderId = id
        return folder
    }

    fun updateFolder(folder: QuizFolder) {
        val index = folders.indexOfFirst { it.id == folder.id }
        if (index >= 0) folders[index] = folder
    }

    fun deleteFolder(folderId: String) {
        folders.removeAll { it.id == folderId }
        quizSets.remove(folderId)
        rankingsByFolder.remove(folderId)
        if (activeFolderId == folderId) {
            activeFolderId = folders.minByOrNull { it.sortOrder }?.id.orEmpty()
        }
    }

    fun getQuizSet(folderId: String): QuizSet =
        quizSets[folderId] ?: error("Quiz set not found for folder: $folderId")

    fun saveQuizSet(quizSet: QuizSet) {
        quizSets[quizSet.id] = quizSet
        val folderIndex = folders.indexOfFirst { it.id == quizSet.id }
        if (folderIndex >= 0) {
            folders[folderIndex] = folders[folderIndex].copy(name = quizSet.title)
        }
    }

    fun getActiveFolderId(): String = activeFolderId.ifEmpty {
        folders.firstOrNull()?.id.orEmpty()
    }

    fun setActiveFolderId(folderId: String) {
        require(folders.any { it.id == folderId }) { "Unknown folder: $folderId" }
        activeFolderId = folderId
    }

    fun seedFolder(folder: QuizFolder, quizSet: QuizSet, demoRankings: List<RankingEntry> = emptyList()) {
        if (folders.none { it.id == folder.id }) folders += folder
        quizSets[folder.id] = quizSet
        rankingsByFolder.getOrPut(folder.id) { mutableListOf() }.apply {
            if (isEmpty() && demoRankings.isNotEmpty()) addAll(demoRankings)
        }
        if (activeFolderId.isEmpty()) activeFolderId = folder.id
    }

    fun rankingsFor(folderId: String): MutableList<RankingEntry> =
        rankingsByFolder.getOrPut(folderId) { mutableListOf() }
}
