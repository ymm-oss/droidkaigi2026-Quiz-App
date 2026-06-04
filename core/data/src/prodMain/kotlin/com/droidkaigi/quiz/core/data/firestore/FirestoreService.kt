package com.droidkaigi.quiz.core.data.firestore

interface FirestoreService {
    suspend fun listFolders(): List<Pair<String, FolderFirestoreDocument>>
    suspend fun getFolder(folderId: String): FolderFirestoreDocument?
    suspend fun setFolder(folderId: String, document: FolderFirestoreDocument)
    suspend fun deleteFolder(folderId: String)
    suspend fun getAppConfig(): AppConfigFirestoreDocument?
    suspend fun setAppConfig(document: AppConfigFirestoreDocument)
    suspend fun addRanking(folderId: String, document: RankingFirestoreDocument)
    suspend fun listRankingsForDate(folderId: String, dateKey: String): List<RankingFirestoreDocument>
}
