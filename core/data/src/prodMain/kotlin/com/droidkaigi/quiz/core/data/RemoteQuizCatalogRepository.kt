package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import dev.zacsweers.metro.ContributesBinding

@ContributesBinding(AppScope::class)
class RemoteQuizCatalogRepository : QuizCatalogRepository {
    override suspend fun listFolders(): List<QuizFolder> = notImplemented()
    override suspend fun createFolder(name: String, description: String): QuizFolder = notImplemented()
    override suspend fun updateFolder(folder: QuizFolder) = notImplemented()
    override suspend fun deleteFolder(folderId: String) = notImplemented()
    override suspend fun getQuizSet(folderId: String): QuizSet = notImplemented()
    override suspend fun saveQuizSet(quizSet: QuizSet) = notImplemented()
    override suspend fun getActiveFolderId(): String = notImplemented()
    override suspend fun setActiveFolderId(folderId: String) = notImplemented()

    private fun notImplemented(): Nothing = error(
        "RemoteQuizCatalogRepository is not implemented. Connect your API in :core:data prodMain.",
    )
}
