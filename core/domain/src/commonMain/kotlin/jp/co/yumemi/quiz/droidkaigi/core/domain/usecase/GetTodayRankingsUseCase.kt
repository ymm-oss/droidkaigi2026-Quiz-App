package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository

class GetTodayRankingsUseCase(private val rankingRepository: RankingRepository) {
    suspend operator fun invoke(folderId: String): List<RankingEntry> = rankingRepository.getTodayRankings(folderId)
}
