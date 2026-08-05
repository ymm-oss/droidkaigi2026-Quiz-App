package com.droidkaigi.quiz

import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.rules.ActivityScenarioRule

internal typealias QuizComposeRule =
    AndroidComposeTestRule<ActivityScenarioRule<MainActivity>, MainActivity>

/** CI エミュレータ向けに余裕を持たせた待機上限。 */
internal const val UI_WAIT_MS = 30_000L

internal fun QuizComposeRule.waitUntilText(
    text: String,
    timeoutMillis: Long = UI_WAIT_MS,
    substring: Boolean = false,
) {
    waitUntil(timeoutMillis = timeoutMillis) {
        try {
            onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty()
        } catch (_: IllegalStateException) {
            // Activity / Compose host not ready yet (common on flaky wireless adb).
            false
        }
    }
}

internal fun QuizComposeRule.waitUntilTag(tag: String, timeoutMillis: Long = UI_WAIT_MS) {
    waitUntil(timeoutMillis = timeoutMillis) {
        try {
            onAllNodes(hasTestTag(tag), useUnmergedTree = true)
                .fetchSemanticsNodes()
                .isNotEmpty()
        } catch (_: IllegalStateException) {
            false
        }
    }
}

internal fun QuizComposeRule.startQuizWithNickname(nickname: String) {
    waitUntilText("クイズを始める")
    onNode(hasSetTextAction()).performTextInput(nickname)
    waitForIdle()
    onNodeWithText("クイズを始める").performClick()
    waitForIdle()
    // Markdown プロンプトは分割されることがあるので進捗ラベルで開始を確認する
    waitUntilText("1 / 3")
}

/** ChoiceCard の testTag で選択肢をタップする（プロンプト内の同文言と区別）。 */
internal fun QuizComposeRule.clickChoice(label: String) {
    val tag = "choice:$label"
    waitUntilTag(tag)
    val node = onNodeWithTag(tag, useUnmergedTree = true)
    try {
        node.performScrollTo()
    } catch (_: AssertionError) {
        // Already on-screen, or scroll host not yet linked — still attempt click.
    }
    onNodeWithTag(tag, useUnmergedTree = true).performClick()
    waitForIdle()
}

internal fun QuizComposeRule.clickSubmitAnswer() {
    waitUntilTag("quizSubmit")
    val node = onNodeWithTag("quizSubmit", useUnmergedTree = true)
    try {
        node.performScrollTo()
    } catch (_: AssertionError) {
        // Already on-screen.
    }
    onNodeWithTag("quizSubmit", useUnmergedTree = true).performClick()
    waitForIdle()
}

internal fun QuizComposeRule.waitForAnswerFeedback() {
    waitUntil(timeoutMillis = UI_WAIT_MS) {
        onAllNodes(hasText("正解！")).fetchSemanticsNodes().isNotEmpty() ||
            onAllNodes(hasText("不正解")).fetchSemanticsNodes().isNotEmpty()
    }
}

internal fun QuizComposeRule.proceedAfterFeedback() {
    waitForAnswerFeedback()
    waitUntilTag("feedbackContinue")
    onNodeWithTag("feedbackContinue", useUnmergedTree = true).performClick()
    waitForIdle()
}
