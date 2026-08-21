package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository

class DeleteRankingEntryUseCase(private val rankingRepository: RankingRepository) {
    suspend operator fun invoke(folderId: String, entryId: String) {
        require(entryId.isNotBlank()) { "entryId must not be blank" }
        rankingRepository.deleteEntry(folderId, entryId)
    }
}
