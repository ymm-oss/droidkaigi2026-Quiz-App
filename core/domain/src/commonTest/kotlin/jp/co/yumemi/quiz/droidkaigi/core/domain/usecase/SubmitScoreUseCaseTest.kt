package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.ranking.RankingEntryId
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals

class SubmitScoreUseCaseTest {
    @Test
    fun invoke_passesFixedCompletedAtAndDeterministicEntryId() = runBlocking {
        val calls = mutableListOf<SubmitCall>()
        val useCase = SubmitScoreUseCase(
            rankingRepository = object : RankingRepository {
                override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = emptyList()

                override suspend fun submitScore(
                    result: QuizResult,
                    completedAtEpochMillis: Long,
                    folderId: String,
                    entryId: String,
                ) {
                    calls += SubmitCall(result, completedAtEpochMillis, folderId, entryId)
                }

                override suspend fun deleteEntry(folderId: String, entryId: String) = Unit

                override suspend fun clearTodayRankings(folderId: String) = Unit
            },
        )
        val result = QuizResult("Alice", 2, 3, 2, 10_000)

        useCase(
            result = result,
            folderId = "demo",
            completedAtEpochMillis = 1_700_000_100_000L,
            startedAtEpochMillis = 1_700_000_000_000L,
        )
        useCase(
            result = result,
            folderId = "demo",
            completedAtEpochMillis = 1_700_000_100_000L,
            startedAtEpochMillis = 1_700_000_000_000L,
        )

        assertEquals(2, calls.size)
        val expectedId = RankingEntryId.forSession("demo", "Alice", 1_700_000_000_000L)
        calls.forEach { call ->
            assertEquals(1_700_000_100_000L, call.completedAtEpochMillis)
            assertEquals("demo", call.folderId)
            assertEquals(expectedId, call.entryId)
            assertEquals(2, call.result.score)
        }
    }

    private data class SubmitCall(
        val result: QuizResult,
        val completedAtEpochMillis: Long,
        val folderId: String,
        val entryId: String,
    )
}
