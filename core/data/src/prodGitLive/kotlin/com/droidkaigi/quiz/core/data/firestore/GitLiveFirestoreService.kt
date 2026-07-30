package com.droidkaigi.quiz.core.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore
import kotlin.coroutines.cancellation.CancellationException

internal class GitLiveFirestoreService : FirestoreService {
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

    override suspend fun getFolder(folderId: String): FolderFirestoreDocument? =
        db.collection(FirestorePaths.FOLDERS)
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

    override suspend fun getAppConfig(): AppConfigFirestoreDocument? =
        db.collection(FirestorePaths.APP_CONFIG)
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

    override suspend fun putRanking(folderId: String, entryId: String, document: RankingFirestoreDocument) {
        val ref = db.collection(FirestorePaths.FOLDERS)
            .document(folderId)
            .collection(FirestorePaths.RANKINGS)
            .document(entryId)
        try {
            ref.set(RankingFirestoreDocument.serializer(), document) {
                encodeDefaults = true
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // create-only rules: retry after a successful write looks like a denied update.
            // Only an identical existing document proves that the previous attempt landed.
            val existing = runCatching {
                ref.get()
                    .takeIf { it.exists }
                    ?.data(RankingFirestoreDocument.serializer())
            }.getOrNull()
            if (existing == document) {
                return
            }
            throw e
        }
    }

    override suspend fun listRankingsForDate(folderId: String, dateKey: String): List<RankingFirestoreDocument> {
        val rankings = db.collection(FirestorePaths.FOLDERS)
            .document(folderId)
            .collection(FirestorePaths.RANKINGS)
        val snapshots = try {
            rankings
                .where { "dateKey" equalTo dateKey }
                .orderBy("score", Direction.DESCENDING)
                .get()
                .documents
        } catch (error: Exception) {
            if (!error.isFirestoreMissingCompositeIndexError()) throw error
            // 複合インデックス未デプロイ時のみ: 等値クエリ + クライアント側 score 降順
            rankings
                .where { "dateKey" equalTo dateKey }
                .get()
                .documents
        }
        return snapshots
            .mapNotNull {
                runCatching { it.data(RankingFirestoreDocument.serializer()) }.getOrNull()
            }
            .filter { it.isComplete() && it.dateKey == dateKey }
            .sortedByDescending { it.score }
    }
}
