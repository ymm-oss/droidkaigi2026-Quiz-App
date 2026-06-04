package com.droidkaigi.quiz.core.domain.repository

import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.model.QuizResult

interface RankingRepository {
    suspend fun getTodayRankings(): List<RankingEntry>
    suspend fun submitScore(result: QuizResult, completedAtEpochMillis: Long)
}
