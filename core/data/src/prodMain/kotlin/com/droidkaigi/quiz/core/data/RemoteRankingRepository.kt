package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.data.firestore.FirestoreService
import com.droidkaigi.quiz.core.data.firestore.RankingFirestoreDocument
import com.droidkaigi.quiz.core.data.firestore.toDomain
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.time.todayLocalDate
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
        return firestore.listRankingsForDate(folderId, dateKey).map { it.toDomain() }
    }

    override suspend fun submitScore(
        result: QuizResult,
        completedAtEpochMillis: Long,
        folderId: String,
    ) {
        val dateKey = instantProvider.todayLocalDate().toString()
        firestore.addRanking(
            folderId = folderId,
            document = RankingFirestoreDocument(
                nickname = result.nickname,
                score = result.score,
                completedAtEpochMillis = completedAtEpochMillis,
                dateKey = dateKey,
            ),
        )
    }
}
