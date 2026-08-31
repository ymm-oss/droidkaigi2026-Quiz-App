package jp.co.yumemi.quiz.droidkaigi.core.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.AppConfigStatus
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow

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

    override suspend fun getPublishedFolderIds(): List<String> {
        ensureSeeded()
        return catalog.withLock { getPublishedFolderIds() }
    }

    override suspend fun setPublishedFolderIds(folderIds: List<String>) =
        catalog.withLock { setPublishedFolderIds(folderIds) }

    override suspend fun listPublishedFolders(): List<QuizFolder> {
        ensureSeeded()
        return catalog.withLock {
            val byId = listFolders().associateBy { it.id }
            getPublishedFolderIds().mapNotNull { byId[it] }
        }
    }

    override suspend fun getSitePublished(): Boolean {
        ensureSeeded()
        return catalog.withLock { getSitePublished() }
    }

    override suspend fun setSitePublished(published: Boolean) = catalog.withLock { setSitePublished(published) }

    override fun observeAppConfig(): Flow<AppConfigStatus> = flow {
        ensureSeeded()
        emitAll(catalog.appConfig)
    }
}
