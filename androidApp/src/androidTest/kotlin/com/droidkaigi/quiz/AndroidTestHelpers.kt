package com.droidkaigi.quiz

import androidx.compose.ui.test.hasSetTextAction
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
        onAllNodes(hasText(text, substring = substring)).fetchSemanticsNodes().isNotEmpty()
    }
}

internal fun QuizComposeRule.startQuizWithNickname(nickname: String) {
    waitUntilText("クイズを始める")
    onNode(hasSetTextAction()).performTextInput(nickname)
    waitForIdle()
    onNodeWithText("クイズを始める").performClick()
    waitForIdle()
    // Markdown プロンプトは分割されることがあるので進捗ラベルで開始を確認する
    waitUntilText("0 / 3")
}

/** ChoiceCard の testTag で選択肢をタップする（プロンプト内の同文言と区別）。 */
internal fun QuizComposeRule.clickChoice(label: String) {
    onNodeWithTag("choice:$label").performScrollTo().performClick()
    waitForIdle()
}

internal fun QuizComposeRule.clickSubmitAnswer() {
    onNodeWithText("回答する").performScrollTo().performClick()
    waitForIdle()
}

internal fun QuizComposeRule.waitForAnswerFeedback() {
    waitUntil(timeoutMillis = UI_WAIT_MS) {
        onAllNodes(hasText("正解！")).fetchSemanticsNodes().isNotEmpty() ||
            onAllNodes(hasText("不正解")).fetchSemanticsNodes().isNotEmpty()
    }
}
