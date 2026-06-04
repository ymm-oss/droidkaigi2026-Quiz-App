package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.repository.RankingRepository

class GetTodayRankingsUseCase(
    private val rankingRepository: RankingRepository,
) {
    suspend operator fun invoke(): List<RankingEntry> = rankingRepository.getTodayRankings()
}
