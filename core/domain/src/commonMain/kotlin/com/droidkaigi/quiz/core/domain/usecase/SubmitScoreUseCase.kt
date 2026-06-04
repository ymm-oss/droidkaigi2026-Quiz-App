package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.time.InstantProvider

class SubmitScoreUseCase(
    private val rankingRepository: RankingRepository,
    private val instantProvider: InstantProvider,
) {
    suspend operator fun invoke(result: QuizResult, folderId: String) {
        rankingRepository.submitScore(result, instantProvider.nowEpochMillis(), folderId)
    }
}
