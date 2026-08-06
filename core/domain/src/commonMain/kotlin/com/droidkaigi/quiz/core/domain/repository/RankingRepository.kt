package com.droidkaigi.quiz.core.domain.repository

import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.RankingEntry

interface RankingRepository {
    suspend fun getTodayRankings(folderId: String): List<RankingEntry>

    /**
     * @param entryId deterministic document id for this quiz completion (idempotent retries)
     */
    suspend fun submitScore(
        result: QuizResult,
        completedAtEpochMillis: Long,
        folderId: String,
        entryId: String,
    )

    /** Deletes a single ranking document. */
    suspend fun deleteEntry(folderId: String, entryId: String)

    /** Deletes all ranking documents for the device "today" in [folderId]. */
    suspend fun clearTodayRankings(folderId: String)
}
