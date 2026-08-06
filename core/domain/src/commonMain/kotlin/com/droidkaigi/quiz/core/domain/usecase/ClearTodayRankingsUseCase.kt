package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.repository.RankingRepository

class ClearTodayRankingsUseCase(private val rankingRepository: RankingRepository) {
    suspend operator fun invoke(folderId: String) {
        rankingRepository.clearTodayRankings(folderId)
    }
}
