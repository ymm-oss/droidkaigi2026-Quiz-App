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

@Inject
@ContributesBinding(AppScope::class)
class RemoteRankingRepository(
    private val firestore: FirestoreService,
    private val instantProvider: InstantProvider,
) : RankingRepository {
    override suspend fun getTodayRankings(folderId: String): List<RankingEntry> {
        val dateKey = instantProvider.todayLocalDate().toString()
        return firestore.listRankingsForDate(folderId, dateKey).map { (entryId, document) ->
            document.toDomain(entryId)
        }
    }

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
}
