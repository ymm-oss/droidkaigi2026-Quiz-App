package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.ranking.RankingEntryId
import com.droidkaigi.quiz.core.domain.repository.RankingRepository

class SubmitScoreUseCase(
    private val rankingRepository: RankingRepository,
) {
    /**
     * @param completedAtEpochMillis fixed finish time from the final answer (not retry clock)
     * @param startedAtEpochMillis session start; used for the deterministic ranking document ID
     */
    suspend operator fun invoke(
        result: QuizResult,
        folderId: String,
        completedAtEpochMillis: Long,
        startedAtEpochMillis: Long,
    ) {
        val entryId = RankingEntryId.forSession(
            folderId = folderId,
            nickname = result.nickname,
            startedAtEpochMillis = startedAtEpochMillis,
        )
        rankingRepository.submitScore(
            result = result,
            completedAtEpochMillis = completedAtEpochMillis,
            folderId = folderId,
            entryId = entryId,
        )
    }
}
