package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.domain.model.QuizResult
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
    fun submitScore_appearsInTodayRankings() = runTest {
        val clock = FixedInstantProvider(1_700_000_000_000)
        val repo = FakeRankingRepository(clock)
        val initial = repo.getTodayRankings().size

        repo.submitScore(
            QuizResult("Player1", 2, 3, 250, 30_000),
            clock.nowEpochMillis(),
        )

        val rankings = repo.getTodayRankings()
        assertEquals(initial + 1, rankings.size)
        assertTrue(rankings.any { it.nickname == "Player1" && it.score == 250 })
    }
}
