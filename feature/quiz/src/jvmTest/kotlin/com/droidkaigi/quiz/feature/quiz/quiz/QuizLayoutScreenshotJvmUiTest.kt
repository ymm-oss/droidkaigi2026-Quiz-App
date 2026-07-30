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
 * Deterministic Desktop screenshots for quiz layout (#48/#49).
 * Output: docs/screenshots/quiz/ (PNG collection after PR integration).
 */
class QuizLayoutScreenshotJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureMultipleChoiceWithCodeBlock() = runDesktopComposeUiTest(width = 1080, height = 1920) {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = QuizPreviewFixtures.multipleChoiceState(),
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
        onNodeWithText("2 / 5").assertIsDisplayed()
        captureSurfacePng("04-quiz-multiple-choice-code.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureReorderList() = runDesktopComposeUiTest(width = 1080, height = 1920) {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = QuizPreviewFixtures.reorderState(),
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
        onNodeWithText("3 / 5").assertIsDisplayed()
        captureSurfacePng("05-quiz-reorder.png")
    }
}
