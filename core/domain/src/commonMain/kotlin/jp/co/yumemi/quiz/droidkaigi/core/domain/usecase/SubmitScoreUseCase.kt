package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.ranking.RankingEntryId
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository

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
