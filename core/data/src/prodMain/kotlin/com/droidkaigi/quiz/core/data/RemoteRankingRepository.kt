package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import dev.zacsweers.metro.ContributesBinding

/**
 * Prod ranking source (e.g. Firebase Realtime Database / Firestore). Wire the client SDK here.
 */
@ContributesBinding(AppScope::class)
class RemoteRankingRepository : RankingRepository {
    override suspend fun getTodayRankings(folderId: String): List<RankingEntry> {
        error(
            "RemoteRankingRepository is not implemented. " +
                "Connect Firebase or your API in :core:data prodMain.",
        )
    }

    override suspend fun submitScore(result: QuizResult, completedAtEpochMillis: Long, folderId: String) {
        error(
            "RemoteRankingRepository is not implemented. " +
                "Connect Firebase or your API in :core:data prodMain.",
        )
    }
}
