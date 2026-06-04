package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.data.dto.QuizSetDto
import com.droidkaigi.quiz.core.data.dto.toDomain
import com.droidkaigi.quiz.core.data.generated.resources.Res
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.repository.QuizRepository
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

@ContributesBinding(AppScope::class)
@Inject
class JsonQuizRepository(
    private val catalog: InMemoryQuizCatalog,
    private val instantProvider: InstantProvider,
    private val json: Json = Json { ignoreUnknownKeys = true },
) : QuizRepository {
    private var seeded = false

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getDefaultQuizSet(): QuizSet {
        ensureSeeded()
        val folderId = catalog.withLock { getActiveFolderId() }
        return catalog.withLock { getQuizSet(folderId) }
    }

    @OptIn(ExperimentalResourceApi::class)
    private suspend fun ensureSeeded() {
        if (seeded) return
        catalog.withLock {
            if (listFolders().isNotEmpty()) {
                seeded = true
                return@withLock
            }
            val bytes = Res.readBytes("files/quiz_set.json")
            val text = bytes.decodeToString()
            val dto = json.decodeFromString<QuizSetDto>(text)
            val quizSet = dto.toDomain()
            val now = instantProvider.nowEpochMillis()
            seedFolder(
                folder = QuizFolder(
                    id = quizSet.id,
                    name = "Day 1 — デモ",
                    description = "同梱 JSON（初級）",
                    sortOrder = 0,
                ),
                quizSet = quizSet.copy(
                    questions = quizSet.questions.map { question ->
                        when (question) {
                            is com.droidkaigi.quiz.core.domain.model.SingleChoice ->
                                if (question.explanationMarkdown.isBlank()) {
                                    question.copy(
                                        explanationMarkdown = "**Compose Multiplatform** で UI を共有できます。\n- Android / Desktop / iOS など",
                                    )
                                } else {
                                    question
                                }
                            else -> question
                        }
                    },
                ),
                demoRankings = listOf(
                    RankingEntry("KotlinFan", 350, now - 3_600_000),
                    RankingEntry("ComposePro", 320, now - 7_200_000),
                    RankingEntry("NavExplorer", 290, now - 10_800_000),
                ),
            )
            seedFolder(
                folder = QuizFolder(
                    id = "day2-intermediate",
                    name = "Day 2 — 中級",
                    description = "会場午後枠（空セット）",
                    sortOrder = 1,
                ),
                quizSet = QuizSet(
                    id = "day2-intermediate",
                    title = "Day 2 — 中級",
                    questions = emptyList(),
                ),
            )
            seeded = true
        }
    }
}
