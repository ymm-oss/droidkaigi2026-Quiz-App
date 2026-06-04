package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.time.isSameDay
import com.droidkaigi.quiz.core.domain.time.todayLocalDate
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class FakeRankingRepository(
    private val instantProvider: InstantProvider,
    private val catalog: InMemoryQuizCatalog,
) : RankingRepository {
    override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = catalog.withLock {
        val today = instantProvider.todayLocalDate()
        rankingsFor(folderId)
            .filter { isSameDay(it.completedAtEpochMillis, today) }
            .sortedByDescending { it.score }
    }

    override suspend fun submitScore(
        result: QuizResult,
        completedAtEpochMillis: Long,
        folderId: String,
    ) = catalog.withLock {
        rankingsFor(folderId) += RankingEntry(
            nickname = result.nickname,
            score = result.score,
            completedAtEpochMillis = completedAtEpochMillis,
        )
    }
}
