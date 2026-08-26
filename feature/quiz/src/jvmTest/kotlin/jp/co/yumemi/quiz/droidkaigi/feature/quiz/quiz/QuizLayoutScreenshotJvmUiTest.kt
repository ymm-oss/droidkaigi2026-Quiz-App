package jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocale
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.captureSurfacePng
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.preview.QuizPreviewFixtures
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
        onNode(
            hasTestTag("choice:count の変更で UI が再 Composition される")
                .and(SemanticsMatcher.expectValue(SemanticsProperties.Role, Role.Checkbox)),
        ).assertIsDisplayed()
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
