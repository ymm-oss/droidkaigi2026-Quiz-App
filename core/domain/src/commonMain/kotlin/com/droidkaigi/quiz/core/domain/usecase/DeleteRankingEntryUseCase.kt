package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.repository.RankingRepository

class DeleteRankingEntryUseCase(private val rankingRepository: RankingRepository) {
    suspend operator fun invoke(folderId: String, entryId: String) {
        require(entryId.isNotBlank()) { "entryId must not be blank" }
        rankingRepository.deleteEntry(folderId, entryId)
    }
}
