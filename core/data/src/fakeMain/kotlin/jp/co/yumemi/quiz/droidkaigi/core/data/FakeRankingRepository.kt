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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow

@ContributesBinding(AppScope::class)
@Inject
class FakeRankingRepository(private val instantProvider: InstantProvider, private val catalog: InMemoryQuizCatalog) :
    RankingRepository {
    private val submittedEntryKeys = mutableSetOf<String>()
    private val rankingsRevision = MutableStateFlow(0)

    override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = catalog.withLock {
        todayRankingsLocked(folderId)
    }

    override fun observeTodayRankings(folderId: String): Flow<List<RankingEntry>> = flow {
        rankingsRevision.collect {
            emit(getTodayRankings(folderId))
        }
    }

    override suspend fun submitScore(
        result: QuizResult,
        completedAtEpochMillis: Long,
        folderId: String,
        entryId: String,
    ) {
        val added = catalog.withLock {
            val key = "$folderId/$entryId"
            if (key in submittedEntryKeys) return@withLock false
            submittedEntryKeys += key
            rankingsFor(folderId) += RankingEntry(
                nickname = result.nickname,
                score = result.score,
                completedAtEpochMillis = completedAtEpochMillis,
                id = entryId,
            )
            true
        }
        if (added) bumpRankings()
    }

    override suspend fun deleteEntry(folderId: String, entryId: String) {
        catalog.withLock {
            rankingsFor(folderId).removeAll { it.id == entryId }
            submittedEntryKeys.remove("$folderId/$entryId")
        }
        bumpRankings()
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
        bumpRankings()
    }

    private fun InMemoryQuizCatalog.todayRankingsLocked(folderId: String): List<RankingEntry> {
        val today = instantProvider.todayLocalDate()
        return rankingsFor(folderId)
            .filter { isSameDay(it.completedAtEpochMillis, today) }
            .sortedByDescending { it.score }
    }

    private fun bumpRankings() {
        rankingsRevision.value += 1
    }
}
