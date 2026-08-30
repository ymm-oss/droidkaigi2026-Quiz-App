package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.Source
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

internal class GitLiveFirestoreService : BaseFirestoreService() {
    private val db get() = Firebase.firestore

    override suspend fun listFolders(): List<Pair<String, FolderFirestoreDocument>> =
        db.collection(FirestorePaths.FOLDERS)
            .get(Source.SERVER)
            .documents
            .mapNotNull { snapshot ->
                runCatching {
                    snapshot.data(FolderListFirestoreDocument.serializer()).withResolvedLabels()
                }.getOrNull()?.let { summary ->
                    snapshot.id to summary.toFolderFirestoreDocument()
                }
            }

    override suspend fun getFolder(folderId: String): FolderFirestoreDocument? {
        val snapshot = db.collection(FirestorePaths.FOLDERS)
            .document(folderId)
            .get(Source.SERVER)
        if (!snapshot.exists) return null
        return snapshot.data(FolderFirestoreDocument.serializer())?.withResolvedLabels()
    }

    override suspend fun setFolder(folderId: String, document: FolderFirestoreDocument) {
        db.collection(FirestorePaths.FOLDERS)
            .document(folderId)
            .setAndAwaitServer(FolderFirestoreDocument.serializer(), document)
    }

    override suspend fun deleteFolder(folderId: String) {
        // Firestore does not cascade-delete subcollections; clear rankings first.
        deleteAllRankingsInFolder(folderId)
        val folderRef = db.collection(FirestorePaths.FOLDERS).document(folderId)
        folderRef.delete()
        folderRef.awaitServerDeleted()
    }

    override suspend fun getAppConfig(): AppConfigFirestoreDocument? {
        val snapshot = db.collection(FirestorePaths.APP_CONFIG)
            .document(FirestorePaths.APP_CONFIG_DEFAULT)
            .get(Source.SERVER)
        if (!snapshot.exists) return null
        return snapshot.data(AppConfigFirestoreDocument.serializer())
    }

    override suspend fun setAppConfig(document: AppConfigFirestoreDocument) {
        db.collection(FirestorePaths.APP_CONFIG)
            .document(FirestorePaths.APP_CONFIG_DEFAULT)
            .setAndAwaitServer(AppConfigFirestoreDocument.serializer(), document)
    }

    override fun observeAppConfig(): Flow<AppConfigFirestoreDocument?> =
        db.collection(FirestorePaths.APP_CONFIG)
            .document(FirestorePaths.APP_CONFIG_DEFAULT)
            .snapshots
            .map { snapshot ->
                if (!snapshot.exists) {
                    null
                } else {
                    snapshot.data(AppConfigFirestoreDocument.serializer())
                }
            }

    override suspend fun getStaffAppRelease(): StaffAppReleaseFirestoreDocument? {
        val snapshot = db.collection(FirestorePaths.STAFF_APP_RELEASE)
            .document(FirestorePaths.STAFF_APP_RELEASE_LATEST)
            .get(Source.SERVER)
        if (!snapshot.exists) return null
        return snapshot.data(StaffAppReleaseFirestoreDocument.serializer())
            .takeIf { it.isComplete() }
    }

    override suspend fun getRanking(folderId: String, entryId: String): RankingFirestoreDocument? {
        val snapshot = rankingsCollection(folderId)
            .document(entryId)
            .get(Source.SERVER)
        if (!snapshot.exists) return null
        return snapshot.data(RankingFirestoreDocument.serializer())
    }

    override suspend fun setRanking(folderId: String, entryId: String, document: RankingFirestoreDocument) {
        rankingsCollection(folderId)
            .document(entryId)
            .setAndAwaitServer(RankingFirestoreDocument.serializer(), document)
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
            .get(Source.SERVER)
            .documents
            .mapNotNull { snapshot ->
                runCatching { snapshot.data(RankingFirestoreDocument.serializer()) }
                    .getOrNull()
                    ?.let { snapshot.id to it }
            }
    }

    override fun observeQueryRankings(
        folderId: String,
        dateKey: String,
    ): Flow<List<Pair<String, RankingFirestoreDocument>>> =
        rankingsCollection(folderId)
            .where { "dateKey" equalTo dateKey }
            .snapshots
            .map { querySnapshot ->
                querySnapshot.documents.mapNotNull { snapshot ->
                    runCatching { snapshot.data(RankingFirestoreDocument.serializer()) }
                        .getOrNull()
                        ?.let { snapshot.id to it }
                }
            }

    override fun isMissingCompositeIndexError(error: Throwable): Boolean = error.isFirestoreMissingCompositeIndexError()

    override suspend fun deleteRanking(folderId: String, entryId: String) {
        val rankingRef = rankingsCollection(folderId).document(entryId)
        rankingRef.delete()
        rankingRef.awaitServerDeleted()
    }

    private suspend fun deleteAllRankingsInFolder(folderId: String) {
        val rankings = rankingsCollection(folderId)
        repeat(MAX_CLEAR_PASSES) {
            val documents = rankings.get().documents
            if (documents.isEmpty()) return
            documents.forEach { snapshot ->
                deleteRanking(folderId, snapshot.id)
            }
        }
        if (rankings.get().documents.isNotEmpty()) {
            error("Could not clear all rankings for folder $folderId")
        }
    }

    private fun rankingsCollection(folderId: String) = db.collection(FirestorePaths.FOLDERS)
        .document(folderId)
        .collection(FirestorePaths.RANKINGS)

    private companion object {
        private const val MAX_CLEAR_PASSES = 5
    }
}
