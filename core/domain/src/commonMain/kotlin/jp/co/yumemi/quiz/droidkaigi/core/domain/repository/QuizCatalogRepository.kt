package jp.co.yumemi.quiz.droidkaigi.core.domain.repository

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet

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
}
