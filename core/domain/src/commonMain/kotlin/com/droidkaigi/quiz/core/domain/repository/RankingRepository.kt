package com.droidkaigi.quiz.core.domain.repository

import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.RankingEntry

interface RankingRepository {
    suspend fun getTodayRankings(folderId: String): List<RankingEntry>
    suspend fun submitScore(result: QuizResult, completedAtEpochMillis: Long, folderId: String)
}
