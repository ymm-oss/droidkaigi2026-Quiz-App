package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.StaffAuthHolder

internal actual fun initializeFirebasePlatform() = Unit

internal actual fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService =
    UnsupportedFirestoreService()

private class UnsupportedFirestoreService : FirestoreService {
    private fun unsupported(): Nothing = error(
        "Wasm の prod ランタイムでは Firestore 未対応です。Android または Desktop（JVM）で " +
            "-Pquiz.runtime=prod を使用するか、quiz.runtime=fake にしてください。",
    )

    override suspend fun listFolders(): List<Pair<String, FolderFirestoreDocument>> = unsupported()

    override suspend fun getFolder(folderId: String): FolderFirestoreDocument? = unsupported()

    override suspend fun setFolder(folderId: String, document: FolderFirestoreDocument) = unsupported()

    override suspend fun deleteFolder(folderId: String) = unsupported()

    override suspend fun getAppConfig(): AppConfigFirestoreDocument? = unsupported()

    override suspend fun setAppConfig(document: AppConfigFirestoreDocument) = unsupported()

    override suspend fun addRanking(folderId: String, document: RankingFirestoreDocument) = unsupported()

    override suspend fun listRankingsForDate(folderId: String, dateKey: String): List<RankingFirestoreDocument> =
        unsupported()
}
