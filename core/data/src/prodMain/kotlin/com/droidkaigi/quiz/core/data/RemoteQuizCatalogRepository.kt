package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.data.firestore.AppConfigFirestoreDocument
import com.droidkaigi.quiz.core.data.firestore.FirestoreDiagnostics
import com.droidkaigi.quiz.core.data.firestore.FirestoreService
import com.droidkaigi.quiz.core.data.firestore.FolderFirestoreDocument
import com.droidkaigi.quiz.core.data.firestore.toQuizFolder
import com.droidkaigi.quiz.core.data.firestore.toQuizSet
import com.droidkaigi.quiz.core.data.firestore.toFirestoreDocument
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

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
        if (existing.isEmpty()) {
            firestore.setAppConfig(AppConfigFirestoreDocument(activeFolderId = folderId, updatedAtEpochMillis = now))
        }
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
                updatedAtEpochMillis = now,
            ),
        )
    }

    override suspend fun deleteFolder(folderId: String) {
        firestore.deleteFolder(folderId)
        val config = firestore.getAppConfig()
        if (config?.activeFolderId == folderId) {
            val fallback = listFolders().firstOrNull()?.id.orEmpty()
            if (fallback.isNotEmpty()) {
                firestore.setAppConfig(
                    AppConfigFirestoreDocument(
                        activeFolderId = fallback,
                        updatedAtEpochMillis = instantProvider.nowEpochMillis(),
                    ),
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

    override suspend fun getActiveFolderId(): String {
        val configured = firestore.getAppConfig()?.activeFolderId?.takeIf { it.isNotBlank() }
        if (configured != null && firestore.getFolder(configured) != null) return configured
        val folders = listFolders()
        val firstFolder = folders.firstOrNull()?.id
        if (firstFolder != null) {
            FirestoreDiagnostics.log(
                "QuizCatalog",
                "getActiveFolderId: appConfig missing/invalid; fallback folderId=$firstFolder",
            )
            return firstFolder
        }
        FirestoreDiagnostics.logError(
            "QuizCatalog",
            "getActiveFolderId: no published quiz. " +
                "Need folders + appConfig/default. " +
                "Local: firebase emulators:start --import=./emulator-data — docs/DEVELOPMENT.md#firebase-emulatorlocal " +
                "(configuredActiveFolderId=${configured ?: "null"}, folderCount=${folders.size})",
        )
        error(MISSING_QUIZ_DATA_MESSAGE)
    }

    companion object {
        /** UI 向けの短い文言。手順の詳細は [FirestoreDiagnostics] に出す。 */
        const val MISSING_QUIZ_DATA_MESSAGE = "公開中の問題がありません"
    }

    override suspend fun setActiveFolderId(folderId: String) {
        require(firestore.getFolder(folderId) != null) { "Unknown folder: $folderId" }
        firestore.setAppConfig(
            AppConfigFirestoreDocument(
                activeFolderId = folderId,
                updatedAtEpochMillis = instantProvider.nowEpochMillis(),
            ),
        )
    }
}
