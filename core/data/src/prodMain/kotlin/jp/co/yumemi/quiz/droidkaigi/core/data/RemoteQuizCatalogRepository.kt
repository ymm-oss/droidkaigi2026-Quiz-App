package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.AppConfigFirestoreDocument
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FirestoreDiagnostics
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FirestoreService
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FolderFirestoreDocument
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.toQuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.toQuizSet
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.toFirestoreDocument
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.PublishedFolderIds
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.AppConfigStatus
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(AppScope::class)
class RemoteQuizCatalogRepository(
    private val firestore: FirestoreService,
    private val instantProvider: InstantProvider,
) : QuizCatalogRepository {
    override suspend fun listFolders(): List<QuizFolder> {
        FirestoreDiagnostics.log("QuizCatalog", "listFolders")
        val folders = firestore.listFolders()
            .map { (id, doc) -> doc.toQuizFolder(id) }
            .sortedBy { it.sortOrder }
        FirestoreDiagnostics.log(
            "QuizCatalog",
            "listFolders result count=${folders.size} ids=${folders.map { "${it.id}:${it.displayName}" }}",
        )
        return folders
    }

    override suspend fun createFolder(name: String, description: String): QuizFolder {
        val trimmedName = name.trim()
        val existing = listFolders()
        val folderId = newFolderDocumentId()
        val now = instantProvider.nowEpochMillis()
        val document = FolderFirestoreDocument(
            name = trimmedName,
            description = description.trim(),
            sortOrder = existing.size,
            title = trimmedName,
            questions = emptyList(),
            updatedAtEpochMillis = now,
        )
        FirestoreDiagnostics.log("QuizCatalog", "createFolder writing folderId=$folderId name=$trimmedName")
        firestore.setFolder(folderId, document)
        FirestoreDiagnostics.log("QuizCatalog", "createFolder wrote folderId=$folderId")
        return document.toQuizFolder(folderId)
    }

    override suspend fun updateFolder(folder: QuizFolder) {
        val current = firestore.getFolder(folder.id)
            ?: error("フォルダが見つかりません: ${folder.id}")
        val now = instantProvider.nowEpochMillis()
        firestore.setFolder(
            folder.id,
            current.copy(
                name = folder.name,
                description = folder.description,
                sortOrder = folder.sortOrder,
                publicName = folder.publicName,
                publicDescription = folder.publicDescription,
                useInternalAsPublic = folder.useInternalAsPublic,
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun deleteFolder(folderId: String) {
        firestore.deleteFolder(folderId)
        val published = firestore.getAppConfig()?.resolvedPublishedFolderIds().orEmpty()
        if (folderId in published) {
            writeAppConfig { current ->
                current.withPublishedFolderIds(
                    folderIds = current.resolvedPublishedFolderIds().filter { it != folderId },
                    updatedAtEpochMillis = instantProvider.nowEpochMillis(),
                )
            }
        }
    }

    override suspend fun getQuizSet(folderId: String): QuizSet {
        val document = firestore.getFolder(folderId)
            ?: error("クイズが見つかりません（folderId=$folderId）。Firestore の folders/$folderId を確認してください。")
        return document.toQuizSet(folderId)
    }

    override suspend fun saveQuizSet(quizSet: QuizSet) {
        val folderId = quizSet.id
        val current = firestore.getFolder(folderId)
        val now = instantProvider.nowEpochMillis()
        val baseFolder = current?.toQuizFolder(folderId)
            ?: QuizFolder(id = folderId, name = quizSet.title, description = "", sortOrder = listFolders().size)
        firestore.setFolder(folderId, baseFolder.toFirestoreDocument(quizSet, now))
    }

    override suspend fun getActiveFolderId(): String =
        getPublishedFolderIds().firstOrNull()
            ?: error(
                "公開中のフォルダがありません。Firestore の appConfig/default.publishedFolderIds を確認してください。",
            )

    override suspend fun setActiveFolderId(folderId: String) {
        setPublishedFolderIds(listOf(folderId))
    }

    override suspend fun getPublishedFolderIds(): List<String> {
        val ids = firestore.getAppConfig()?.resolvedPublishedFolderIds().orEmpty()
        return ids.filter { firestore.getFolder(it) != null }
    }

    override suspend fun setPublishedFolderIds(folderIds: List<String>) {
        val cleaned = PublishedFolderIds.resolve(
            publishedFolderIds = folderIds,
            activeFolderId = "",
        )
        cleaned.forEach { id ->
            require(firestore.getFolder(id) != null) { "Unknown folder: $id" }
        }
        writeAppConfig { current ->
            current.withPublishedFolderIds(cleaned, instantProvider.nowEpochMillis())
        }
    }

    override suspend fun listPublishedFolders(): List<QuizFolder> {
        val ids = firestore.getAppConfig()?.resolvedPublishedFolderIds().orEmpty()
        return ids.mapNotNull { id ->
            firestore.getFolder(id)?.toQuizFolder(id)
        }
    }

    override suspend fun getSitePublished(): Boolean =
        firestore.getAppConfig()?.sitePublished ?: false

    override fun observeAppConfig(): Flow<AppConfigStatus> =
        firestore.observeAppConfig().map { document ->
            val published = document?.resolvedPublishedFolderIds().orEmpty()
            AppConfigStatus(
                sitePublished = document?.sitePublished ?: false,
                activeFolderId = published.firstOrNull() ?: document?.activeFolderId.orEmpty(),
                publishedFolderIds = published,
            )
        }

    override suspend fun setSitePublished(published: Boolean) {
        writeAppConfig { current ->
            current.copy(
                sitePublished = published,
                updatedAtEpochMillis = instantProvider.nowEpochMillis(),
            )
        }
    }

    /** Preserves other [AppConfigFirestoreDocument] fields across partial updates. */
    private suspend fun writeAppConfig(transform: (AppConfigFirestoreDocument) -> AppConfigFirestoreDocument) {
        val current = firestore.getAppConfig() ?: AppConfigFirestoreDocument()
        firestore.setAppConfig(transform(current))
    }
}
