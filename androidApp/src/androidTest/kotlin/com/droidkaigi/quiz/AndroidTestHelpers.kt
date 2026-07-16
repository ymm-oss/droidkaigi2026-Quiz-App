package com.droidkaigi.quiz

import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.AndroidComposeTestRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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

/** プロンプト内の同文言と区別するため、クリック可能な選択肢だけをタップする。 */
internal fun QuizComposeRule.clickChoice(label: String) {
    onNode(hasText(label) and hasClickAction()).performClick()
    waitForIdle()
}
