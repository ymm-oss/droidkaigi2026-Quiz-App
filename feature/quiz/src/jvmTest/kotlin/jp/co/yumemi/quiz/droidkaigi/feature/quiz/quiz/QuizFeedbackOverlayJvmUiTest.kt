package jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocale
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.preview.QuizPreviewFixtures
import kotlin.test.Test
import kotlin.test.assertTrue

/**
 * JVM Compose UI: answer feedback overlay is visible regardless of scroll content.
 */
class QuizFeedbackOverlayJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun correctFeedback_showsOverlayAndNextButton() = runComposeUiTest {
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

        onNodeWithTag("answerFeedbackOverlay", useUnmergedTree = true).assertIsDisplayed()
        onNodeWithText("正解！").assertIsDisplayed()
        onNodeWithText("次の問題へ").assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun finishingFeedback_showsSeeResultsAndInvokesContinue() = runComposeUiTest {
        var continued = false
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = QuizPreviewFixtures.singleChoiceState(
                                showFeedback = true,
                                lastAnswerCorrect = false,
                                isFinishing = true,
                            ),
                            onSelectSingle = {},
                            onToggleMultiple = {},
                            onMoveReorder = { _, _ -> },
                            onSubmitAnswer = {},
                            onContinueAfterFeedback = { continued = true },
                        )
                    }
                }
            }
        }

        onNodeWithText("不正解").assertIsDisplayed()
        onNodeWithText("結果を見る").assertIsDisplayed()
        onNodeWithTag("feedbackContinue", useUnmergedTree = true).performClick()
        assertTrue(continued)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun correctFeedback_doesNotFlashIncorrectDuringDismiss() = runComposeUiTest {
        mainClock.autoAdvance = false
        var state by mutableStateOf(
            QuizPreviewFixtures.singleChoiceState(
                showFeedback = true,
                lastAnswerCorrect = true,
            ),
        )
        setContent {
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

        mainClock.advanceTimeByFrame()
        onNodeWithText("正解！").assertIsDisplayed()

        state = QuizPreviewFixtures.singleChoiceState(
            showFeedback = false,
            lastAnswerCorrect = null,
        )
        mainClock.advanceTimeByFrame()

        onAllNodesWithText("不正解").assertCountEquals(0)
    }
}
