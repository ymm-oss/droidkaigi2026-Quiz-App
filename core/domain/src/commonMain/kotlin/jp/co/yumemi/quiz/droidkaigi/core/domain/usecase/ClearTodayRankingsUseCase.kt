package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository

class ClearTodayRankingsUseCase(private val rankingRepository: RankingRepository) {
    suspend operator fun invoke(folderId: String) {
        rankingRepository.clearTodayRankings(folderId)
    }
}
