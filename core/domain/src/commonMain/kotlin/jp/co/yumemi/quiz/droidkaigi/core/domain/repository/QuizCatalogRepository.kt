package jp.co.yumemi.quiz.droidkaigi.core.domain.repository

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.AppConfigStatus
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import kotlinx.coroutines.flow.Flow

interface QuizCatalogRepository {
    suspend fun listFolders(): List<QuizFolder>
    suspend fun createFolder(name: String, description: String = ""): QuizFolder
    suspend fun updateFolder(folder: QuizFolder)
    suspend fun deleteFolder(folderId: String)
    suspend fun getQuizSet(folderId: String): QuizSet
    suspend fun saveQuizSet(quizSet: QuizSet)
    suspend fun getActiveFolderId(): String
    suspend fun setActiveFolderId(folderId: String)
    suspend fun getSitePublished(): Boolean
    suspend fun setSitePublished(published: Boolean)

    /** Real-time `appConfig/default`. Missing document → unpublished + empty folder id. */
    fun observeAppConfig(): Flow<AppConfigStatus>

    /**
     * Participant-visible folder IDs. Default maps the legacy single [getActiveFolderId].
     * Prod/fake implementations persist a list and keep `activeFolderId` as the first entry.
     */
    suspend fun getPublishedFolderIds(): List<String> {
        val active = runCatching { getActiveFolderId() }.getOrNull().orEmpty()
        return if (active.isBlank()) emptyList() else listOf(active)
    }

    suspend fun setPublishedFolderIds(folderIds: List<String>) {
        folderIds.firstOrNull { it.isNotBlank() }?.let { setActiveFolderId(it) }
    }

    /** Metadata for published folders, in publish-list order. Does not require a full catalog list. */
    suspend fun listPublishedFolders(): List<QuizFolder> {
        val ids = getPublishedFolderIds()
        if (ids.isEmpty()) return emptyList()
        val byId = listFolders().associateBy { it.id }
        return ids.mapNotNull { byId[it] }
    }
}
