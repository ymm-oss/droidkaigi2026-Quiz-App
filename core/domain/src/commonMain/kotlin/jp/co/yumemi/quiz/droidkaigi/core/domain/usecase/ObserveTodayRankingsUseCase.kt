package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository
import kotlinx.coroutines.flow.Flow

class ObserveTodayRankingsUseCase(private val rankingRepository: RankingRepository) {
    operator fun invoke(folderId: String): Flow<List<RankingEntry>> =
        rankingRepository.observeTodayRankings(folderId)
}
