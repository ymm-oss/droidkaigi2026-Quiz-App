package com.droidkaigi.quiz.core.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.firestore.Direction
import dev.gitlive.firebase.firestore.firestore

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

    override suspend fun addRanking(folderId: String, document: RankingFirestoreDocument) {
        db.collection(FirestorePaths.FOLDERS)
            .document(folderId)
            .collection(FirestorePaths.RANKINGS)
            .add(RankingFirestoreDocument.serializer(), document) {
                encodeDefaults = true
            }
    }

    override suspend fun listRankingsForDate(folderId: String, dateKey: String): List<RankingFirestoreDocument> {
        val snapshots = try {
            db.collection(FirestorePaths.FOLDERS)
                .document(folderId)
                .collection(FirestorePaths.RANKINGS)
                .where { "dateKey" equalTo dateKey }
                .orderBy("score", Direction.DESCENDING)
                .get()
                .documents
        } catch (_: Exception) {
            db.collection(FirestorePaths.FOLDERS)
                .document(folderId)
                .collection(FirestorePaths.RANKINGS)
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
