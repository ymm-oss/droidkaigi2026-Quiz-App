package com.droidkaigi.quiz.feature.quiz.quiz

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.droidkaigi.quiz.core.ui.locale.LocalAppLocale
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.feature.quiz.captureSurfacePng
import com.droidkaigi.quiz.feature.quiz.preview.QuizPreviewFixtures
import kotlin.test.Test

/**
 * Captures quiz answer-feedback overlay screenshots for PR review.
 * Output: docs/screenshots/quiz/
 */
class QuizFeedbackScreenshotJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureCorrectFeedbackOverlay() = runDesktopComposeUiTest(width = 1080, height = 1920) {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = QuizPreviewFixtures.singleChoiceState(
                                showFeedback = true,
                                lastAnswerCorrect = true,
                            ),
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
        onNodeWithText("正解！").assertIsDisplayed()
        onNodeWithText("次の問題へ").assertIsDisplayed()
        captureSurfacePng("01-feedback-correct.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureIncorrectFeedbackOverlay() = runDesktopComposeUiTest(width = 1080, height = 1920) {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = QuizPreviewFixtures.singleChoiceState(
                                selectedId = "a",
                                showFeedback = true,
                                lastAnswerCorrect = false,
                            ),
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
        onNodeWithText("不正解").assertIsDisplayed()
        onNodeWithText("次の問題へ").assertIsDisplayed()
        captureSurfacePng("02-feedback-incorrect.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureFinishingFeedbackOverlay() = runDesktopComposeUiTest(width = 1080, height = 1920) {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = QuizPreviewFixtures.singleChoiceState(
                                showFeedback = true,
                                lastAnswerCorrect = true,
                                isFinishing = true,
                            ),
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
        onNodeWithText("正解！").assertIsDisplayed()
        onNodeWithText("結果を見る").assertIsDisplayed()
        captureSurfacePng("03-feedback-finish.png")
    }
}
