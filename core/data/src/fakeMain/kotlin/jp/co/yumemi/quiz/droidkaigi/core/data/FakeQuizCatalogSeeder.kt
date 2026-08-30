package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.dto.QuizSetDto
import jp.co.yumemi.quiz.droidkaigi.core.data.dto.toDomain
import jp.co.yumemi.quiz.droidkaigi.core.data.generated.resources.Res
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import kotlinx.serialization.json.Json
import org.jetbrains.compose.resources.ExperimentalResourceApi

/**
 * fake 開発用: 同梱 [quiz_set.json] をインメモリ catalog に投入する（prod では未使用）。
 */
@OptIn(ExperimentalResourceApi::class)
internal object FakeQuizCatalogSeeder {
    private var seeded = false

    suspend fun ensureSeeded(
        catalog: InMemoryQuizCatalog,
        instantProvider: InstantProvider,
        json: Json = Json { ignoreUnknownKeys = true },
    ) {
        if (seeded) return
        catalog.withLock {
            if (listFolders().isNotEmpty()) {
                seeded = true
                return@withLock
            }
            seedBundledCatalog(json, instantProvider)
            seeded = true
        }
    }

    private suspend fun InMemoryQuizCatalog.seedBundledCatalog(json: Json, instantProvider: InstantProvider) {
        val bytes = Res.readBytes("files/quiz_set.json")
        val text = bytes.decodeToString()
        val dto = json.decodeFromString<QuizSetDto>(text)
        val quizSet = dto.toDomain()
        val now = instantProvider.nowEpochMillis()
        seedFolder(
            folder = QuizFolder(
                id = quizSet.id,
                name = "一般向け",
                description = "会場向け（初級）",
                sortOrder = 0,
            ),
            quizSet = quizSet.copy(
                questions = quizSet.questions.map { question ->
                    when (question) {
                        is SingleChoice ->
                            if (question.explanationMarkdown.isBlank()) {
                                question.copy(
                                    explanationMarkdown =
                                    "**Compose Multiplatform** で UI を共有できます。\n- Android / Desktop / iOS など",
                                )
                            } else {
                                question
                            }

                        else -> question
                    }
                },
            ),
            demoRankings = listOf(
                RankingEntry("KotlinFan", 100, now - 3_600_000, id = "seed-kotlinfan", totalCount = 3),
                RankingEntry("ComposePro", 72, now - 7_200_000, id = "seed-composepro", totalCount = 3),
                RankingEntry("NavExplorer", 50, now - 10_800_000, id = "seed-navexplorer", totalCount = 3),
            ),
        )
        seedFolder(
            folder = QuizFolder(
                id = "day1-hard",
                name = "高難易度",
                description = "上級者向け",
                sortOrder = 1,
            ),
            quizSet = quizSet.copy(id = "day1-hard", title = "高難易度"),
        )
        seedFolder(
            folder = QuizFolder(
                id = "day2-intermediate",
                name = "Day 2 — 中級",
                description = "会場午後枠（空セット）",
                sortOrder = 2,
            ),
            quizSet = QuizSet(
                id = "day2-intermediate",
                title = "Day 2 — 中級",
                questions = emptyList(),
            ),
        )
        setPublishedFolderIds(listOf(quizSet.id, "day1-hard"))
        // Fake harness: open the site so participant Home Start works without staff toggle.
        setSitePublished(true)
    }
}
