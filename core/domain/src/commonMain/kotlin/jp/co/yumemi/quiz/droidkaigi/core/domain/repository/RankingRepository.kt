package jp.co.yumemi.quiz.droidkaigi.core.domain.repository

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry

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
