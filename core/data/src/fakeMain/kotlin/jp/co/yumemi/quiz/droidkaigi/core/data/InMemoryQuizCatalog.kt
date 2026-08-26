package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.PublishedFolderIds
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
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
    private val publishedFolderIds = mutableListOf<String>()

    /** Fake default after seed is true for easier local play-through; unset catalog stays false. */
    private var sitePublished: Boolean = false

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
        if (publishedFolderIds.isEmpty()) publishedFolderIds += id
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
        publishedFolderIds.removeAll { it == folderId }
    }

    fun getQuizSet(folderId: String): QuizSet = quizSets[folderId] ?: error("Quiz set not found for folder: $folderId")

    /** Folder metadata (name / description) is owned by [updateFolder]; saving questions must not touch it. */
    fun saveQuizSet(quizSet: QuizSet) {
        quizSets[quizSet.id] = quizSet
    }

    fun getPublishedFolderIds(): List<String> =
        PublishedFolderIds.resolve(publishedFolderIds, activeFolderId = "")
            .filter { id -> folders.any { it.id == id } }

    fun setPublishedFolderIds(folderIds: List<String>) {
        val cleaned = PublishedFolderIds.resolve(folderIds, activeFolderId = "")
        cleaned.forEach { id -> require(folders.any { it.id == id }) { "Unknown folder: $id" } }
        publishedFolderIds.clear()
        publishedFolderIds += cleaned
    }

    fun getActiveFolderId(): String = getPublishedFolderIds().firstOrNull().orEmpty()

    fun setActiveFolderId(folderId: String) {
        setPublishedFolderIds(listOf(folderId))
    }

    fun getSitePublished(): Boolean = sitePublished

    fun setSitePublished(published: Boolean) {
        sitePublished = published
    }

    fun seedFolder(folder: QuizFolder, quizSet: QuizSet, demoRankings: List<RankingEntry> = emptyList()) {
        if (folders.none { it.id == folder.id }) folders += folder
        quizSets[folder.id] = quizSet
        rankingsByFolder.getOrPut(folder.id) { mutableListOf() }.apply {
            if (isEmpty() && demoRankings.isNotEmpty()) addAll(demoRankings)
        }
        if (publishedFolderIds.isEmpty()) publishedFolderIds += folder.id
    }

    fun rankingsFor(folderId: String): MutableList<RankingEntry> =
        rankingsByFolder.getOrPut(folderId) { mutableListOf() }
}
