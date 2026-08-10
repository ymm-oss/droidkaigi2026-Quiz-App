package com.droidkaigi.quiz.core.data.firestore

interface FirestoreService {
    suspend fun listFolders(): List<Pair<String, FolderFirestoreDocument>>
    suspend fun getFolder(folderId: String): FolderFirestoreDocument?
    suspend fun setFolder(folderId: String, document: FolderFirestoreDocument)
    suspend fun deleteFolder(folderId: String)
    suspend fun getAppConfig(): AppConfigFirestoreDocument?
    suspend fun setAppConfig(document: AppConfigFirestoreDocument)
    suspend fun getStaffAppRelease(): StaffAppReleaseFirestoreDocument?
    /**
     * Writes [document] at a fixed [entryId]. If the document already exists (e.g. a prior
     * attempt succeeded but the client timed out), treat as success without updating.
     */
    suspend fun putRanking(folderId: String, entryId: String, document: RankingFirestoreDocument)
    suspend fun listRankingsForDate(folderId: String, dateKey: String): List<Pair<String, RankingFirestoreDocument>>
    suspend fun deleteRanking(folderId: String, entryId: String)
    suspend fun deleteRankingsForDate(folderId: String, dateKey: String)
}
