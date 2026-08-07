package com.droidkaigi.quiz.core.data.firestore

import kotlin.coroutines.cancellation.CancellationException

/**
 * SDK 非依存の Firestore 共通ロジック。
 *
 * ランキング周りのセキュリティルール対応（create-only の冪等リトライ）や
 * 複合インデックス未デプロイ時のフォールバックは SDK によらず同じ挙動が必要なため、
 * ここに集約する。プラットフォーム実装（GitLive / Firebase JS SDK）は
 * raw な読み書きプリミティブとエラー判定だけを提供すればよい。
 */
internal abstract class BaseFirestoreService : FirestoreService {

    /** ランキング 1 件の生取得（存在しない・デコード不能なら null）。 */
    protected abstract suspend fun getRanking(folderId: String, entryId: String): RankingFirestoreDocument?

    /** ランキング 1 件の生書き込み（リトライ処理なし）。 */
    protected abstract suspend fun setRanking(folderId: String, entryId: String, document: RankingFirestoreDocument)

    /**
     * `dateKey` 等値クエリでランキングを取得する。
     * [orderByScoreDescending] が true の場合は `score` 降順を付ける（複合インデックスが必要）。
     * デコードできないドキュメントは除外して返す。
     */
    protected abstract suspend fun queryRankings(
        folderId: String,
        dateKey: String,
        orderByScoreDescending: Boolean,
    ): List<Pair<String, RankingFirestoreDocument>>

    /** SDK 固有の例外から複合インデックス不足エラーかどうかを判定する。 */
    protected abstract fun isMissingCompositeIndexError(error: Throwable): Boolean

    final override suspend fun putRanking(folderId: String, entryId: String, document: RankingFirestoreDocument) {
        try {
            setRanking(folderId, entryId, document)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            // create-only rules: retry after a successful write looks like a denied update.
            // Only an identical existing document proves that the previous attempt landed.
            val existing = runCatching { getRanking(folderId, entryId) }.getOrNull()
            if (existing == document) {
                return
            }
            throw e
        }
    }

    final override suspend fun listRankingsForDate(
        folderId: String,
        dateKey: String,
    ): List<Pair<String, RankingFirestoreDocument>> {
        val entries = try {
            queryRankings(folderId, dateKey, orderByScoreDescending = true)
        } catch (e: CancellationException) {
            throw e
        } catch (error: Exception) {
            if (!isMissingCompositeIndexError(error)) throw error
            // 複合インデックス未デプロイ時のみ: 等値クエリ + クライアント側 score 降順
            queryRankings(folderId, dateKey, orderByScoreDescending = false)
        }
        return entries
            .filter { (_, document) -> document.isComplete() && document.dateKey == dateKey }
            .sortedByDescending { it.second.score }
    }

    final override suspend fun deleteRankingsForDate(folderId: String, dateKey: String) {
        repeat(MAX_CLEAR_PASSES) {
            val entries = listRankingsForDate(folderId, dateKey)
            if (entries.isEmpty()) return
            entries.forEach { (entryId, _) ->
                deleteRanking(folderId, entryId)
            }
        }
        if (listRankingsForDate(folderId, dateKey).isNotEmpty()) {
            error("Could not clear all rankings for $dateKey")
        }
    }

    private companion object {
        private const val MAX_CLEAR_PASSES = 5
    }
}
