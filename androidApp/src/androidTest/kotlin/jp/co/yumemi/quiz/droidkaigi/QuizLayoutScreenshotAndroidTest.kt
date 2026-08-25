package jp.co.yumemi.quiz.droidkaigi

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocale
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.preview.QuizPreviewFixtures
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz.QuizContent
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz.QuizUiState
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz.SubmitPhase
import jp.co.yumemi.quiz.droidkaigi.feature.ranking.RankingContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Deterministic Android screenshots for quiz layout (#48/#49): Markdown code blocks,
 * multi-choice, and reorder lists. PNG collection is done after PR integration.
 *
 * See `.cursor/skills/android-compose-screenshot/SKILL.md`.
 */
@RunWith(AndroidJUnit4::class)
class QuizLayoutScreenshotAndroidTest {
    @get:Rule
    val composeRule = createComposeRule()

    private fun outputDir(): File {
        val fromAgp = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val dir = if (fromAgp != null) {
            File(fromAgp)
        } else {
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
                ?: error("No external files dir")
        }
        dir.mkdirs()
        return dir
    }

    private fun capture(fileName: String) {
        val bitmap = runCatching {
            composeRule.onRoot().captureToImage().asAndroidBitmap()
        }.getOrElse {
            // AlertDialog has a second Compose root; capture the instrumented display in that case.
            InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
        }
        FileOutputStream(File(outputDir(), fileName)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun render(state: QuizUiState) {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = state,
                            onSelectSingle = {},
                            onToggleMultiple = {},
                            onMoveReorder = { _, _ -> },
                            onSubmitAnswer = {},
                            onContinueAfterFeedback = {},
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun captureMultipleChoiceWithCodeBlock() {
        render(QuizPreviewFixtures.multipleChoiceState())
        composeRule.onNode(
            hasTestTag("choice:count の変更で UI が再 Composition される")
                .and(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)),
        ).assertIsDisplayed()
        capture("android-quiz-multiple-choice-code.png")
    }

    @Test
    fun captureReorderList() {
        render(QuizPreviewFixtures.reorderState())
        capture("android-quiz-reorder.png")
    }

    @Test
    fun captureSingleChoiceWithCodeBlock() {
        render(QuizPreviewFixtures.singleChoiceState())
        capture("android-quiz-single-choice-code.png")
    }

    @Test
    fun captureScoreSubmitFailure() {
        render(
            QuizPreviewFixtures.multipleChoiceState().copy(
                showFeedback = true,
                lastAnswerCorrect = true,
                isFinishing = true,
                submitPhase = SubmitPhase.Failed,
            ),
        )
        capture("android-quiz-score-submit-failed.png")
    }

    @Test
    fun captureRankingRefreshFailureWithStaleEntries() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        RankingContent(
                            entries = listOf(
                                RankingEntry("Alice", 250, 1_700_000_000_000L),
                                RankingEntry("Bob", 180, 1_700_000_100_000L),
                            ),
                            highlightNickname = "Alice",
                            isLoading = false,
                            errorMessage = "ランキングを更新できませんでした。",
                            onRetryClick = {},
                            onGoHomeClick = {},
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        capture("android-ranking-refresh-failed.png")
    }
}
