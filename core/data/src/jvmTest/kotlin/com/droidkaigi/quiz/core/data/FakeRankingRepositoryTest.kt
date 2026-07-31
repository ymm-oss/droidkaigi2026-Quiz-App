package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.ranking.RankingEntryId
import com.droidkaigi.quiz.core.domain.time.InstantProvider
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
}
