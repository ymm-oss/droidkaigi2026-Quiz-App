package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.isSameDay
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.todayLocalDate
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppScope::class)
@Inject
class FakeRankingRepository(private val instantProvider: InstantProvider, private val catalog: InMemoryQuizCatalog) :
    RankingRepository {
    private val submittedEntryKeys = mutableSetOf<String>()

    override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = catalog.withLock {
        val today = instantProvider.todayLocalDate()
        rankingsFor(folderId)
            .filter { isSameDay(it.completedAtEpochMillis, today) }
            .sortedWith(compareByDescending<RankingEntry> { it.score }.thenBy { it.completedAtEpochMillis })
    }

    override suspend fun submitScore(
        result: QuizResult,
        completedAtEpochMillis: Long,
        folderId: String,
        entryId: String,
    ) = catalog.withLock {
        val key = "$folderId/$entryId"
        if (key in submittedEntryKeys) return@withLock
        submittedEntryKeys += key
        rankingsFor(folderId) += RankingEntry(
            nickname = result.nickname,
            score = result.score,
            completedAtEpochMillis = completedAtEpochMillis,
            id = entryId,
            totalCount = result.totalCount,
        )
    }

    override suspend fun deleteEntry(folderId: String, entryId: String) {
        catalog.withLock {
            rankingsFor(folderId).removeAll { it.id == entryId }
            submittedEntryKeys.remove("$folderId/$entryId")
        }
    }

    override suspend fun clearTodayRankings(folderId: String) {
        catalog.withLock {
            val today = instantProvider.todayLocalDate()
            val list = rankingsFor(folderId)
            val toRemove = list.filter { isSameDay(it.completedAtEpochMillis, today) }
            toRemove.forEach { entry ->
                if (entry.id.isNotBlank()) {
                    submittedEntryKeys.remove("$folderId/${entry.id}")
                }
            }
            list.removeAll { isSameDay(it.completedAtEpochMillis, today) }
        }
    }
}
