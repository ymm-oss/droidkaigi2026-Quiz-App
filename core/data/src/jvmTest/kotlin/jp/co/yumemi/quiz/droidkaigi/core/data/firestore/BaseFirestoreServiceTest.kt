package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

/**
 * SDK 非依存の共通ロジック（冪等リトライ・インデックス不足フォールバック・複数パス削除）の検証。
 * prod ランタイム時のみコンパイルされる（`./gradlew :core:data:jvmTest -Pquiz.runtime=prod`）。
 */
class BaseFirestoreServiceTest {

    @Test
    fun putRanking_succeedsWhenIdenticalDocumentAlreadyExists() = runTest {
        val document = rankingDocument(nickname = "alice", score = 10)
        val service = TestFirestoreService(
            onSetRanking = { error("create-only rules deny update") },
            onGetRanking = { document },
        )

        // 前回の書き込みが成功していた場合（同一ドキュメントが存在）は成功扱い
        service.putRanking("folder", "entry", document)
    }

    @Test
    fun putRanking_propagatesCancellationFromRecoveryRead() = runTest {
        val document = rankingDocument(nickname = "alice", score = 10)
        val service = TestFirestoreService(
            onSetRanking = { error("create-only rules deny update") },
            onGetRanking = { throw CancellationException() },
        )

        assertFailsWith<CancellationException> {
            service.putRanking("folder", "entry", document)
        }
    }

    @Test
    fun putRanking_rethrowsWhenExistingDocumentDiffers() = runTest {
        val service = TestFirestoreService(
            onSetRanking = { error("permission denied") },
            onGetRanking = { rankingDocument(nickname = "someone-else", score = 1) },
        )

        assertFailsWith<IllegalStateException> {
            service.putRanking("folder", "entry", rankingDocument(nickname = "alice", score = 10))
        }
    }

    @Test
    fun listRankingsForDate_fallsBackToUnorderedQueryOnMissingIndex() = runTest {
        val service = TestFirestoreService(
            onQueryRankings = { _, _, orderByScoreDescending ->
                if (orderByScoreDescending) throw MissingIndexException()
                listOf(
                    "low" to rankingDocument(nickname = "low", score = 1),
                    "high" to rankingDocument(nickname = "high", score = 99),
                    "incomplete" to rankingDocument(nickname = "", score = 5),
                    "otherDay" to rankingDocument(nickname = "other", score = 50, dateKey = "2026-01-01"),
                )
            },
            isMissingIndex = { it is MissingIndexException },
        )

        val entries = service.listRankingsForDate("folder", DATE_KEY)

        // フォールバック時はクライアント側で score 降順、必須フィールド欠落と別日付は除外
        assertEquals(listOf("high", "low"), entries.map { it.first })
    }

    @Test
    fun listRankingsForDate_propagatesNonIndexErrors() = runTest {
        val service = TestFirestoreService(
            onQueryRankings = { _, _, _ -> error("unavailable") },
            isMissingIndex = { false },
        )

        assertFailsWith<IllegalStateException> {
            service.listRankingsForDate("folder", DATE_KEY)
        }
    }

    @Test
    fun deleteRankingsForDate_deletesUntilEmpty() = runTest {
        val remaining = mutableListOf(
            "a" to rankingDocument(nickname = "a", score = 1),
            "b" to rankingDocument(nickname = "b", score = 2),
        )
        val service = TestFirestoreService(
            onQueryRankings = { _, _, _ -> remaining.toList() },
        )
        service.onDeleteRanking = { entryId -> remaining.removeAll { it.first == entryId } }

        service.deleteRankingsForDate("folder", DATE_KEY)

        assertTrue(remaining.isEmpty())
        // listRankingsForDate は score 降順で返すため b(2) → a(1) の順に削除される
        assertEquals(listOf("b", "a"), service.deletedEntryIds)
    }

    @Test
    fun deleteRankingsForDate_failsWhenEntriesNeverClear() = runTest {
        val service = TestFirestoreService(
            onQueryRankings = { _, _, _ -> listOf("stuck" to rankingDocument(nickname = "stuck", score = 1)) },
        )

        assertFailsWith<IllegalStateException> {
            service.deleteRankingsForDate("folder", DATE_KEY)
        }
    }

    private fun rankingDocument(
        nickname: String,
        score: Int,
        dateKey: String = DATE_KEY,
    ) = RankingFirestoreDocument(
        nickname = nickname,
        score = score,
        completedAtEpochMillis = 0L,
        dateKey = dateKey,
    )

    private class MissingIndexException : Exception("The query requires an index")

    private class TestFirestoreService(
        private val onSetRanking: suspend () -> Unit = {},
        private val onGetRanking: suspend () -> RankingFirestoreDocument? = { null },
        private val onQueryRankings: suspend (
            folderId: String,
            dateKey: String,
            orderByScoreDescending: Boolean,
        ) -> List<Pair<String, RankingFirestoreDocument>> = { _, _, _ -> emptyList() },
        private val isMissingIndex: (Throwable) -> Boolean = { false },
    ) : BaseFirestoreService() {
        var onDeleteRanking: suspend (entryId: String) -> Unit = {}
        val deletedEntryIds = mutableListOf<String>()

        override suspend fun getRanking(folderId: String, entryId: String): RankingFirestoreDocument? =
            onGetRanking()

        override suspend fun setRanking(folderId: String, entryId: String, document: RankingFirestoreDocument) {
            onSetRanking()
        }

        override suspend fun queryRankings(
            folderId: String,
            dateKey: String,
            orderByScoreDescending: Boolean,
        ): List<Pair<String, RankingFirestoreDocument>> =
            onQueryRankings(folderId, dateKey, orderByScoreDescending)

        override fun isMissingCompositeIndexError(error: Throwable): Boolean = isMissingIndex(error)

        override suspend fun deleteRanking(folderId: String, entryId: String) {
            deletedEntryIds += entryId
            onDeleteRanking(entryId)
        }

        override suspend fun listFolders(): List<Pair<String, FolderFirestoreDocument>> = unused()

        override suspend fun getFolder(folderId: String): FolderFirestoreDocument? = unused()

        override suspend fun setFolder(folderId: String, document: FolderFirestoreDocument) = unused()

        override suspend fun deleteFolder(folderId: String) = unused()

        override suspend fun getAppConfig(): AppConfigFirestoreDocument? = unused()

        override suspend fun setAppConfig(document: AppConfigFirestoreDocument) = unused()

        override suspend fun getStaffAppRelease(): StaffAppReleaseFirestoreDocument? = unused()

        private fun unused(): Nothing = error("not used in this test")
    }

    private companion object {
        private const val DATE_KEY = "2026-08-07"
    }
}
