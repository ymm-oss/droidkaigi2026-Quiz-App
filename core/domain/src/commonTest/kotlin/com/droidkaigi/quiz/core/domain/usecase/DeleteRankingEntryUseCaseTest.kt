package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class DeleteRankingEntryUseCaseTest {
    @Test
    fun invoke_delegatesToRepository() = runBlocking {
        val deleted = mutableListOf<Pair<String, String>>()
        val useCase = DeleteRankingEntryUseCase(
            rankingRepository = object : RankingRepository {
                override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = emptyList()
                override suspend fun submitScore(
                    result: QuizResult,
                    completedAtEpochMillis: Long,
                    folderId: String,
                    entryId: String,
                ) = Unit
                override suspend fun deleteEntry(folderId: String, entryId: String) {
                    deleted += folderId to entryId
                }
                override suspend fun clearTodayRankings(folderId: String) = Unit
            },
        )

        useCase("folder-1", "entry-1")

        assertEquals(listOf("folder-1" to "entry-1"), deleted)
    }

    @Test
    fun invoke_rejectsBlankEntryId() {
        runBlocking {
            val useCase = DeleteRankingEntryUseCase(
                rankingRepository = object : RankingRepository {
                    override suspend fun getTodayRankings(folderId: String): List<RankingEntry> = emptyList()
                    override suspend fun submitScore(
                        result: QuizResult,
                        completedAtEpochMillis: Long,
                        folderId: String,
                        entryId: String,
                    ) = Unit
                    override suspend fun deleteEntry(folderId: String, entryId: String) = Unit
                    override suspend fun clearTodayRankings(folderId: String) = Unit
                },
            )

            assertFailsWith<IllegalArgumentException> {
                useCase("folder-1", "")
            }
        }
    }
}
