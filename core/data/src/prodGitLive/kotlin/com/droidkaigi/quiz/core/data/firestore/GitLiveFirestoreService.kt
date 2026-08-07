package com.droidkaigi.quiz.core.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore

internal class GitLiveFirestoreService : BaseFirestoreService() {
    private val db get() = Firebase.firestore

    override suspend fun listFolders(): List<Pair<String, FolderFirestoreDocument>> =
        db.collection(FirestorePaths.FOLDERS)
            .get()
            .documents
            .mapNotNull { snapshot ->
                runCatching {
                    snapshot.data(FolderListFirestoreDocument.serializer()).withResolvedLabels()
                }.getOrNull()?.let { summary ->
                    snapshot.id to summary.toFolderFirestoreDocument()
                }
            }

    override suspend fun getFolder(folderId: String): FolderFirestoreDocument? = db.collection(FirestorePaths.FOLDERS)
        .document(folderId)
        .get()
        .data(FolderFirestoreDocument.serializer())
        ?.withResolvedLabels()

    override suspend fun setFolder(folderId: String, document: FolderFirestoreDocument) {
        db.collection(FirestorePaths.FOLDERS)
            .document(folderId)
            .set(FolderFirestoreDocument.serializer(), document) {
                encodeDefaults = true
            }
    }

    override suspend fun deleteFolder(folderId: String) {
        db.collection(FirestorePaths.FOLDERS).document(folderId).delete()
    }

    override suspend fun getAppConfig(): AppConfigFirestoreDocument? = db.collection(FirestorePaths.APP_CONFIG)
        .document(FirestorePaths.APP_CONFIG_DEFAULT)
        .get()
        .data(AppConfigFirestoreDocument.serializer())

    override suspend fun setAppConfig(document: AppConfigFirestoreDocument) {
        db.collection(FirestorePaths.APP_CONFIG)
            .document(FirestorePaths.APP_CONFIG_DEFAULT)
            .set(AppConfigFirestoreDocument.serializer(), document) {
                encodeDefaults = true
            }
    }

    override suspend fun getRanking(folderId: String, entryId: String): RankingFirestoreDocument? =
        rankingsCollection(folderId)
            .document(entryId)
            .get()
            .takeIf { it.exists }
            ?.data(RankingFirestoreDocument.serializer())

    override suspend fun setRanking(folderId: String, entryId: String, document: RankingFirestoreDocument) {
        rankingsCollection(folderId)
            .document(entryId)
            .set(RankingFirestoreDocument.serializer(), document) {
                encodeDefaults = true
            }
    }

    override suspend fun queryRankings(
        folderId: String,
        dateKey: String,
        orderByScoreDescending: Boolean,
    ): List<Pair<String, RankingFirestoreDocument>> {
        val filtered = rankingsCollection(folderId).where { "dateKey" equalTo dateKey }
        val query = if (orderByScoreDescending) {
            filtered.orderBy("score", Direction.DESCENDING)
        } else {
            filtered
        }
        return query
            .get()
            .documents
            .mapNotNull { snapshot ->
                runCatching { snapshot.data(RankingFirestoreDocument.serializer()) }
                    .getOrNull()
                    ?.let { snapshot.id to it }
            }
    }

    override fun isMissingCompositeIndexError(error: Throwable): Boolean = error.isFirestoreMissingCompositeIndexError()

    override suspend fun deleteRanking(folderId: String, entryId: String) {
        rankingsCollection(folderId)
            .document(entryId)
            .delete()
    }

    private fun rankingsCollection(folderId: String) = db.collection(FirestorePaths.FOLDERS)
        .document(folderId)
        .collection(FirestorePaths.RANKINGS)
}
