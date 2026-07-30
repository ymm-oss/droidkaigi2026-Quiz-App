package com.droidkaigi.quiz

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.ui.locale.LocalAppLocale
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.feature.quiz.preview.QuizPreviewFixtures
import com.droidkaigi.quiz.feature.quiz.quiz.QuizContent
import com.droidkaigi.quiz.feature.quiz.quiz.QuizUiState
import com.droidkaigi.quiz.feature.quiz.quiz.SubmitPhase
import com.droidkaigi.quiz.feature.ranking.RankingContent
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
