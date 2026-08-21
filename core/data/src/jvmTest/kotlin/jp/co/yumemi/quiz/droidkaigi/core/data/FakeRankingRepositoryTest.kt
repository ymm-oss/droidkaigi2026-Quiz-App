package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.ranking.RankingEntryId
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FixedInstantProvider(private var millis: Long) : InstantProvider {
    override fun nowEpochMillis(): Long = millis
    fun advance(by: Long) {
        millis += by
    }
}

class FakeRankingRepositoryTest {
    @Test
    fun submitScore_appearsInTodayRankings_forFolder() = runTest {
        val clock = FixedInstantProvider(1_700_000_000_000)
        val catalog = InMemoryQuizCatalog()
        catalog.withLock {
            createFolder("Test", "")
        }
        val folderId = catalog.withLock { getActiveFolderId() }
        val repo = FakeRankingRepository(clock, catalog)
        val initial = repo.getTodayRankings(folderId).size
        val entryId = RankingEntryId.forSession(folderId, "Player1", 1_700_000_000_000)

        repo.submitScore(
            result = QuizResult("Player1", 2, 3, 250, 30_000),
            completedAtEpochMillis = clock.nowEpochMillis(),
            folderId = folderId,
            entryId = entryId,
        )

        val rankings = repo.getTodayRankings(folderId)
        assertEquals(initial + 1, rankings.size)
        assertTrue(rankings.any { it.nickname == "Player1" && it.score == 250 })
    }

    @Test
    fun submitScore_sameEntryId_isIdempotent() = runTest {
        val clock = FixedInstantProvider(1_700_000_000_000)
        val catalog = InMemoryQuizCatalog()
        catalog.withLock { createFolder("Test", "") }
        val folderId = catalog.withLock { getActiveFolderId() }
        val repo = FakeRankingRepository(clock, catalog)
        val entryId = RankingEntryId.forSession(folderId, "Player1", 1_700_000_000_000)
        val result = QuizResult("Player1", 2, 3, 250, 30_000)

        repo.submitScore(result, clock.nowEpochMillis(), folderId, entryId)
        clock.advance(60_000)
        repo.submitScore(result, 1_700_000_000_000, folderId, entryId)

        assertEquals(1, repo.getTodayRankings(folderId).count { it.nickname == "Player1" })
    }

    @Test
    fun submitScore_differentSessions_createSeparateEntries() = runTest {
        val clock = FixedInstantProvider(1_700_000_000_000)
        val catalog = InMemoryQuizCatalog()
        catalog.withLock { createFolder("Test", "") }
        val folderId = catalog.withLock { getActiveFolderId() }
        val repo = FakeRankingRepository(clock, catalog)
        val result = QuizResult("Player1", 2, 3, 250, 30_000)

        repo.submitScore(
            result,
            clock.nowEpochMillis(),
            folderId,
            RankingEntryId.forSession(folderId, "Player1", 1_700_000_000_000),
        )
        repo.submitScore(
            result,
            clock.nowEpochMillis(),
            folderId,
            RankingEntryId.forSession(folderId, "Player1", 1_700_000_000_001),
        )

        assertEquals(2, repo.getTodayRankings(folderId).count { it.nickname == "Player1" })
    }

    @Test
    fun deleteEntry_removesFromTodayRankings() = runTest {
        val clock = FixedInstantProvider(1_700_000_000_000)
        val catalog = InMemoryQuizCatalog()
        catalog.withLock { createFolder("Test", "") }
        val folderId = catalog.withLock { getActiveFolderId() }
        val repo = FakeRankingRepository(clock, catalog)
        val entryId = RankingEntryId.forSession(folderId, "Player1", 1_700_000_000_000)
        repo.submitScore(
            result = QuizResult("Player1", 2, 3, 250, 30_000),
            completedAtEpochMillis = clock.nowEpochMillis(),
            folderId = folderId,
            entryId = entryId,
        )

        repo.deleteEntry(folderId, entryId)

        assertEquals(0, repo.getTodayRankings(folderId).size)
    }

    @Test
    fun clearTodayRankings_removesOnlyTodayEntries() = runTest {
        val clock = FixedInstantProvider(1_700_000_000_000)
        val catalog = InMemoryQuizCatalog()
        catalog.withLock { createFolder("Test", "") }
        val folderId = catalog.withLock { getActiveFolderId() }
        val repo = FakeRankingRepository(clock, catalog)
        val todayId = RankingEntryId.forSession(folderId, "Today", 1_700_000_000_000)
        val yesterdayMillis = 1_700_000_000_000 - 86_400_000
        repo.submitScore(
            result = QuizResult("Today", 2, 3, 250, 30_000),
            completedAtEpochMillis = clock.nowEpochMillis(),
            folderId = folderId,
            entryId = todayId,
        )
        catalog.withLock {
            rankingsFor(folderId) += RankingEntry(
                nickname = "Yesterday",
                score = 100,
                completedAtEpochMillis = yesterdayMillis,
                id = "yesterday-entry",
            )
        }

        repo.clearTodayRankings(folderId)

        val remaining = catalog.withLock { rankingsFor(folderId).toList() }
        assertEquals(1, remaining.size)
        assertEquals("Yesterday", remaining.single().nickname)
        assertEquals(0, repo.getTodayRankings(folderId).size)
    }
}
