@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.firebasejs.DocumentSnapshotJs
import com.droidkaigi.quiz.core.data.firebasejs.QuerySnapshotJs
import com.droidkaigi.quiz.core.data.firebasejs.collection
import com.droidkaigi.quiz.core.data.firebasejs.deleteDoc
import com.droidkaigi.quiz.core.data.firebasejs.doc
import com.droidkaigi.quiz.core.data.firebasejs.getDoc
import com.droidkaigi.quiz.core.data.firebasejs.getDocs
import com.droidkaigi.quiz.core.data.firebasejs.jsErrorCodeOrNull
import com.droidkaigi.quiz.core.data.firebasejs.jsErrorMessageOrNull
import com.droidkaigi.quiz.core.data.firebasejs.jsonParse
import com.droidkaigi.quiz.core.data.firebasejs.jsonStringify
import com.droidkaigi.quiz.core.data.firebasejs.orderBy
import com.droidkaigi.quiz.core.data.firebasejs.query
import com.droidkaigi.quiz.core.data.firebasejs.setDoc
import com.droidkaigi.quiz.core.data.firebasejs.toKotlinList
import com.droidkaigi.quiz.core.data.firebasejs.where
import kotlinx.coroutines.await
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlin.js.JsException

/**
 * Firebase JS SDK（modular API）による [FirestoreService] の wasm 実装。
 * GitLive 実装と同じ共通ロジックは [BaseFirestoreService] に集約されている。
 */
internal class FirebaseJsFirestoreService : BaseFirestoreService() {
    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val db get() = FirebaseJsApp.firestore

    override suspend fun listFolders(): List<Pair<String, FolderFirestoreDocument>> =
        getDocs(collection(db, FirestorePaths.FOLDERS)).await<QuerySnapshotJs>().docs.toKotlinList()
            .mapNotNull { snapshot ->
                runCatching {
                    decode(FolderListFirestoreDocument.serializer(), snapshot)?.withResolvedLabels()
                }.getOrNull()?.let { summary ->
                    snapshot.id to summary.toFolderFirestoreDocument()
                }
            }

    override suspend fun getFolder(folderId: String): FolderFirestoreDocument? = decode(
        FolderFirestoreDocument.serializer(),
        getDoc(doc(db, folderPath(folderId))).await<DocumentSnapshotJs>(),
    )?.withResolvedLabels()

    override suspend fun setFolder(folderId: String, document: FolderFirestoreDocument) {
        setDoc(
            doc(db, folderPath(folderId)),
            encode(FolderFirestoreDocument.serializer(), document),
        ).await<JsAny?>()
    }

    override suspend fun deleteFolder(folderId: String) {
        // Firestore does not cascade-delete subcollections; clear rankings first.
        deleteAllRankingsInFolder(folderId)
        deleteDoc(doc(db, folderPath(folderId))).await<JsAny?>()
    }

    override suspend fun getAppConfig(): AppConfigFirestoreDocument? = decode(
        AppConfigFirestoreDocument.serializer(),
        getDoc(doc(db, APP_CONFIG_PATH)).await<DocumentSnapshotJs>(),
    )

    override suspend fun setAppConfig(document: AppConfigFirestoreDocument) {
        setDoc(
            doc(db, APP_CONFIG_PATH),
            encode(AppConfigFirestoreDocument.serializer(), document),
        ).await<JsAny?>()
    }

    override suspend fun getStaffAppRelease(): StaffAppReleaseFirestoreDocument? = decode(
        StaffAppReleaseFirestoreDocument.serializer(),
        getDoc(doc(db, STAFF_APP_RELEASE_PATH)).await<DocumentSnapshotJs>(),
    )?.takeIf { it.isComplete() }

    override suspend fun getRanking(folderId: String, entryId: String): RankingFirestoreDocument? = decode(
        RankingFirestoreDocument.serializer(),
        getDoc(doc(db, rankingPath(folderId, entryId))).await<DocumentSnapshotJs>(),
    )

    override suspend fun setRanking(folderId: String, entryId: String, document: RankingFirestoreDocument) {
        setDoc(
            doc(db, rankingPath(folderId, entryId)),
            encode(RankingFirestoreDocument.serializer(), document),
        ).await<JsAny?>()
    }

    override suspend fun queryRankings(
        folderId: String,
        dateKey: String,
        orderByScoreDescending: Boolean,
    ): List<Pair<String, RankingFirestoreDocument>> {
        val rankings = collection(db, rankingsPath(folderId))
        val dateFilter = where("dateKey", "==", dateKey.toJsString())
        val rankingsQuery = if (orderByScoreDescending) {
            query(rankings, dateFilter, orderBy("score", "desc"))
        } else {
            query(rankings, dateFilter)
        }
        return getDocs(rankingsQuery).await<QuerySnapshotJs>().docs.toKotlinList()
            .mapNotNull { snapshot ->
                runCatching { decode(RankingFirestoreDocument.serializer(), snapshot) }
                    .getOrNull()
                    ?.let { snapshot.id to it }
            }
    }

    override fun isMissingCompositeIndexError(error: Throwable): Boolean {
        // JsException.message は generic な文言のことがあるため、JS Error 側の
        // code / message を thrownValue から直接取り出して判定する。
        // code は `failed-precondition` 形式なので共通判定に合わせて `_` 区切りへ正規化する。
        var current: Throwable? = error
        while (current != null) {
            val thrown = (current as? JsException)?.thrownValue
            if (thrown != null &&
                FirestoreMissingIndexError.matchesCodeOrMessage(
                    codeName = jsErrorCodeOrNull(thrown)?.replace('-', '_'),
                    message = jsErrorMessageOrNull(thrown) ?: current.message,
                )
            ) {
                return true
            }
            current = current.cause
        }
        return FirestoreMissingIndexError.matches(error)
    }

    override suspend fun deleteRanking(folderId: String, entryId: String) {
        deleteDoc(doc(db, rankingPath(folderId, entryId))).await<JsAny?>()
    }

    private suspend fun deleteAllRankingsInFolder(folderId: String) {
        val rankings = collection(db, rankingsPath(folderId))
        repeat(MAX_CLEAR_PASSES) {
            val documents = getDocs(rankings).await<QuerySnapshotJs>().docs.toKotlinList()
            if (documents.isEmpty()) return
            documents.forEach { snapshot ->
                deleteRanking(folderId, snapshot.id)
            }
        }
        if (getDocs(rankings).await<QuerySnapshotJs>().docs.toKotlinList().isNotEmpty()) {
            error("Could not clear all rankings for folder $folderId")
        }
    }

    private fun <T> encode(serializer: KSerializer<T>, value: T): JsAny =
        checkNotNull(jsonParse(json.encodeToString(serializer, value)))

    private fun <T : Any> decode(serializer: KSerializer<T>, snapshot: DocumentSnapshotJs): T? {
        if (!snapshot.exists()) return null
        val data = snapshot.data() ?: return null
        return json.decodeFromString(serializer, jsonStringify(data))
    }

    private fun folderPath(folderId: String) = "${FirestorePaths.FOLDERS}/$folderId"

    private fun rankingsPath(folderId: String) = "${folderPath(folderId)}/${FirestorePaths.RANKINGS}"

    private fun rankingPath(folderId: String, entryId: String) = "${rankingsPath(folderId)}/$entryId"

    private companion object {
        private const val APP_CONFIG_PATH =
            "${FirestorePaths.APP_CONFIG}/${FirestorePaths.APP_CONFIG_DEFAULT}"
        private const val STAFF_APP_RELEASE_PATH =
            "${FirestorePaths.STAFF_APP_RELEASE}/${FirestorePaths.STAFF_APP_RELEASE_LATEST}"
        private const val MAX_CLEAR_PASSES = 5
    }
}
