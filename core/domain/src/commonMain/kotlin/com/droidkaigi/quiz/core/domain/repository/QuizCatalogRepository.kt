package com.droidkaigi.quiz.core.domain.repository

import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizSet

interface QuizCatalogRepository {
    suspend fun listFolders(): List<QuizFolder>
    suspend fun createFolder(name: String, description: String = ""): QuizFolder
    suspend fun updateFolder(folder: QuizFolder)
    suspend fun deleteFolder(folderId: String)
    suspend fun getQuizSet(folderId: String): QuizSet
    suspend fun saveQuizSet(quizSet: QuizSet)
    suspend fun getActiveFolderId(): String
    suspend fun setActiveFolderId(folderId: String)
}
