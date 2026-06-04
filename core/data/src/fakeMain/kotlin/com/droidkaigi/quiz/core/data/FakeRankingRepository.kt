package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.time.isSameDay
import com.droidkaigi.quiz.core.domain.time.todayLocalDate
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@ContributesBinding(AppScope::class)
class FakeRankingRepository(
    private val instantProvider: InstantProvider,
) : RankingRepository {
    private val mutex = Mutex()
    private val entries = mutableListOf<RankingEntry>()

    init {
        seedDemoEntries()
    }

    private fun seedDemoEntries() {
        val now = instantProvider.nowEpochMillis()
        entries += listOf(
            RankingEntry("KotlinFan", 350, now - 3_600_000),
            RankingEntry("ComposePro", 320, now - 7_200_000),
            RankingEntry("NavExplorer", 290, now - 10_800_000),
        )
    }

    override suspend fun getTodayRankings(): List<RankingEntry> = mutex.withLock {
        val today = instantProvider.todayLocalDate()
        entries
            .filter { isSameDay(it.completedAtEpochMillis, today) }
            .sortedByDescending { it.score }
    }

    override suspend fun submitScore(result: QuizResult, completedAtEpochMillis: Long) = mutex.withLock {
        entries += RankingEntry(
            nickname = result.nickname,
            score = result.score,
            completedAtEpochMillis = completedAtEpochMillis,
        )
    }
}
