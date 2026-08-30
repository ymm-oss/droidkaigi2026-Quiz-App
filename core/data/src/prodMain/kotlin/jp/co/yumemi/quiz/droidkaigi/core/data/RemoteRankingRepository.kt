package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FirestoreService
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.RankingFirestoreDocument
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.toDomain
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.localDateOfEpochMillis
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.todayLocalDate
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map

@Inject
@ContributesBinding(AppScope::class)
class RemoteRankingRepository(
    private val firestore: FirestoreService,
    private val instantProvider: InstantProvider,
) : RankingRepository {
    override suspend fun getTodayRankings(folderId: String): List<RankingEntry> {
        val dateKey = instantProvider.todayLocalDate().toString()
        return firestore.listRankingsForDate(folderId, dateKey)
            .map { (entryId, document) -> document.toDomain(entryId) }
            .sortedWith(compareByDescending<RankingEntry> { it.score }.thenBy { it.completedAtEpochMillis })
    }

    override fun observeTodayRankings(folderId: String): Flow<List<RankingEntry>> {
        @OptIn(ExperimentalCoroutinesApi::class)
        return todayDateKeyFlow().flatMapLatest { dateKey ->
            firestore.observeRankingsForDate(folderId, dateKey).map { entries ->
                entries.map { (entryId, document) -> document.toDomain(entryId) }
                    .sortedWith(compareByDescending<RankingEntry> { it.score }.thenBy { it.completedAtEpochMillis })
            }
        }
    }

    private fun todayDateKeyFlow(): Flow<String> = flow {
        while (true) {
            emit(instantProvider.todayLocalDate().toString())
            delay(DATE_KEY_POLL_MS)
        }
    }.distinctUntilChanged()

    override suspend fun submitScore(
        result: QuizResult,
        completedAtEpochMillis: Long,
        folderId: String,
        entryId: String,
    ) {
        val dateKey = localDateOfEpochMillis(completedAtEpochMillis).toString()
        firestore.putRanking(
            folderId = folderId,
            entryId = entryId,
            document = RankingFirestoreDocument(
                nickname = result.nickname,
                score = result.score,
                completedAtEpochMillis = completedAtEpochMillis,
                dateKey = dateKey,
                totalCount = result.totalCount,
            ),
        )
    }

    override suspend fun deleteEntry(folderId: String, entryId: String) {
        firestore.deleteRanking(folderId, entryId)
    }

    override suspend fun clearTodayRankings(folderId: String) {
        val dateKey = instantProvider.todayLocalDate().toString()
        firestore.deleteRankingsForDate(folderId, dateKey)
    }

    private companion object {
        private const val DATE_KEY_POLL_MS = 60_000L
    }
}
