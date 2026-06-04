package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class InMemoryQuizCatalogRepository(
    private val catalog: InMemoryQuizCatalog,
    private val instantProvider: InstantProvider,
) : QuizCatalogRepository {
    private suspend fun ensureSeeded() {
        FakeQuizCatalogSeeder.ensureSeeded(catalog, instantProvider)
    }

    override suspend fun listFolders(): List<QuizFolder> {
        ensureSeeded()
        return catalog.withLock { listFolders() }
    }

    override suspend fun createFolder(name: String, description: String): QuizFolder =
        catalog.withLock { createFolder(name, description) }

    override suspend fun updateFolder(folder: QuizFolder) = catalog.withLock { updateFolder(folder) }

    override suspend fun deleteFolder(folderId: String) = catalog.withLock { deleteFolder(folderId) }

    override suspend fun getQuizSet(folderId: String): QuizSet {
        ensureSeeded()
        return catalog.withLock { getQuizSet(folderId) }
    }

    override suspend fun saveQuizSet(quizSet: QuizSet) = catalog.withLock { saveQuizSet(quizSet) }

    override suspend fun getActiveFolderId(): String {
        ensureSeeded()
        return catalog.withLock { getActiveFolderId() }
    }

    override suspend fun setActiveFolderId(folderId: String) = catalog.withLock { setActiveFolderId(folderId) }
}
