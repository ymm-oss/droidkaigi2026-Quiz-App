package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.StaffAuthHolder
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

internal class RestFirestoreService(
    config: FirebaseProjectConfig,
    staffAuthHolder: StaffAuthHolder,
) : FirestoreService {
    private val client = FirestoreRestClient(
        projectId = config.projectId,
        apiKey = config.apiKey,
        idTokenProvider = { staffAuthHolder.firebaseIdToken },
    )

    override suspend fun listFolders(): List<Pair<String, FolderFirestoreDocument>> =
        client.listDocuments(FirestorePaths.FOLDERS).mapNotNull { (id, fields) ->
            val document = FirestoreJsonCodec.decode(fields, FolderFirestoreDocument.serializer())
                ?: return@mapNotNull null
            id to document
        }

    override suspend fun getFolder(folderId: String): FolderFirestoreDocument? {
        val fields = client.getDocument("${FirestorePaths.FOLDERS}/$folderId") ?: return null
        return FirestoreJsonCodec.decode(fields, FolderFirestoreDocument.serializer())
    }

    override suspend fun setFolder(folderId: String, document: FolderFirestoreDocument) {
        val fields = FirestoreJsonCodec.encode(document, FolderFirestoreDocument.serializer())
        client.setDocument("${FirestorePaths.FOLDERS}/$folderId", fields)
    }

    override suspend fun deleteFolder(folderId: String) {
        client.deleteDocument("${FirestorePaths.FOLDERS}/$folderId")
    }

    override suspend fun getAppConfig(): AppConfigFirestoreDocument? {
        val fields = client.getDocument("${FirestorePaths.APP_CONFIG}/${FirestorePaths.APP_CONFIG_DEFAULT}")
            ?: return null
        return FirestoreJsonCodec.decode(fields, AppConfigFirestoreDocument.serializer())
    }

    override suspend fun setAppConfig(document: AppConfigFirestoreDocument) {
        val fields = FirestoreJsonCodec.encode(document, AppConfigFirestoreDocument.serializer())
        client.setDocument("${FirestorePaths.APP_CONFIG}/${FirestorePaths.APP_CONFIG_DEFAULT}", fields)
    }

    override suspend fun addRanking(folderId: String, document: RankingFirestoreDocument) {
        val fields = FirestoreJsonCodec.encode(document, RankingFirestoreDocument.serializer())
        client.addDocument("${FirestorePaths.FOLDERS}/$folderId", FirestorePaths.RANKINGS, fields)
    }

    override suspend fun listRankingsForDate(folderId: String, dateKey: String): List<RankingFirestoreDocument> {
        val query = buildJsonObject {
            put(
                "from",
                kotlinx.serialization.json.JsonArray(
                    listOf(
                        buildJsonObject {
                            put("collectionId", FirestorePaths.RANKINGS)
                        },
                    ),
                ),
            )
            put(
                "where",
                buildJsonObject {
                    put(
                        "fieldFilter",
                        buildJsonObject {
                            put("field", buildJsonObject { put("fieldPath", "dateKey") })
                            put("op", "EQUAL")
                            put("value", buildJsonObject { put("stringValue", dateKey) })
                        },
                    )
                },
            )
            put(
                "orderBy",
                kotlinx.serialization.json.JsonArray(
                    listOf(
                        buildJsonObject {
                            put("field", buildJsonObject { put("fieldPath", "score") })
                            put("direction", "DESCENDING")
                        },
                    ),
                ),
            )
        }
        val results = runCatching {
            client.runQuery("${FirestorePaths.FOLDERS}/$folderId", query)
        }.getOrElse {
            client.listDocuments("${FirestorePaths.FOLDERS}/$folderId/${FirestorePaths.RANKINGS}")
                .mapNotNull { (_, fields) -> fields }
        }
        return results.mapNotNull { fields ->
            FirestoreJsonCodec.decode(fields, RankingFirestoreDocument.serializer())
        }.filter { it.dateKey == dateKey }
            .sortedByDescending { it.score }
    }
}
