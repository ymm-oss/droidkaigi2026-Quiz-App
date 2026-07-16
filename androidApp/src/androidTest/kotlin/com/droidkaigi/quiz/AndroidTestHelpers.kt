package com.droidkaigi.quiz

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

internal fun QuizComposeRule.waitUntilText(text: String, timeoutMillis: Long = UI_WAIT_MS) {
    waitUntil(timeoutMillis = timeoutMillis) {
        onAllNodes(hasText(text)).fetchSemanticsNodes().isNotEmpty()
    }
}

internal fun QuizComposeRule.startQuizWithNickname(nickname: String) {
    waitUntilText("クイズを始める")
    onNode(hasSetTextAction()).performTextInput(nickname)
    waitForIdle()
    onNodeWithText("クイズを始める").performClick()
    waitForIdle()
}
