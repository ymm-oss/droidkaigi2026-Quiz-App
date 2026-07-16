package com.droidkaigi.quiz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizExitAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun backDuringQuiz_showsExitDialog_cancelKeepsQuiz() {
        startQuiz("ExitCancelTester")
        waitForProgress("0 / 3")

        pressSystemBack()

        composeRule.onNodeWithText("クイズを中断しますか？").assertIsDisplayed()
        composeRule.onNodeWithText("キャンセル").performClick()

        assertExitDialogGone()
        composeRule.onNodeWithText("0 / 3").assertExists()
        composeRule.onNodeWithText("回答する").assertExists()
    }

    @Test
    fun backDuringQuiz_confirmReturnsToHome() {
        startQuiz("ExitConfirmTester")
        waitForProgress("0 / 3")

        pressSystemBack()
        composeRule.onNodeWithText("クイズを中断しますか？").assertIsDisplayed()
        composeRule.onNode(hasText("戻る") and hasAnyAncestor(isDialog())).performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("クイズを始める"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        assertExitDialogGone()
    }

    @Test
    fun abandonThenRestart_doesNotShowExitDialogImmediately() {
        startQuiz("ExitRestartTester")
        waitForProgress("0 / 3")

        pressSystemBack()
        composeRule.onNode(hasText("戻る") and hasAnyAncestor(isDialog())).performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("クイズを始める"))
                .fetchSemanticsNodes().isNotEmpty()
        }

        composeRule.onNodeWithText("クイズを始める").performClick()
        waitForProgress("0 / 3")

        assertExitDialogGone()
    }

    @Test
    fun afterLastAnswer_backDoesNotShowExitDialog() {
        startQuiz("ExitFinishingTester")
        answerThroughLastQuestionSubmit()

        pressSystemBack()

        assertExitDialogGone()
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("クイズ完了"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun assertExitDialogGone() {
        composeRule.waitUntil(timeoutMillis = 2_000) {
            composeRule.onAllNodes(hasText("クイズを中断しますか？"))
                .fetchSemanticsNodes().isEmpty()
        }
    }

    private fun pressSystemBack() {
        composeRule.runOnIdle {
            composeRule.activity.onBackPressedDispatcher.onBackPressed()
        }
        composeRule.waitForIdle()
    }

    private fun startQuiz(nickname: String) {
        composeRule.onNodeWithText("ニックネーム").performTextInput(nickname)
        composeRule.onNodeWithText("クイズを始める").performClick()
    }

    private fun waitForProgress(label: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText(label))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun answerThroughLastQuestionSubmit() {
        waitForProgress("0 / 3")
        composeRule.onNodeWithText("Compose Multiplatform").performClick()
        composeRule.onNodeWithText("回答する").performClick()

        waitForProgress("1 / 3")
        composeRule.onNodeWithText("count の変更で UI が再 Composition される").performClick()
        composeRule.onNodeWithText("Button の onClick はユーザー操作で呼ばれる").performClick()
        composeRule.onNodeWithText("Text の内容は状態に連動して更新される").performClick()
        composeRule.onNodeWithText("回答する").performClick()

        waitForProgress("2 / 3")
        composeRule.onNodeWithText("回答する").performScrollTo().performClick()
        composeRule.waitForIdle()
    }
}
