package com.droidkaigi.quiz

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasAnyAncestor
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isDialog
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
        composeRule.startQuizWithNickname("ExitCancelTester")
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
        composeRule.startQuizWithNickname("ExitConfirmTester")
        waitForProgress("0 / 3")

        pressSystemBack()
        composeRule.onNodeWithText("クイズを中断しますか？").assertIsDisplayed()
        composeRule.onNode(hasText("戻る") and hasAnyAncestor(isDialog())).performClick()

        composeRule.waitUntilText("クイズを始める")
        assertExitDialogGone()
    }

    @Test
    fun abandonThenRestart_doesNotShowExitDialogImmediately() {
        composeRule.startQuizWithNickname("ExitRestartTester")
        waitForProgress("0 / 3")

        pressSystemBack()
        composeRule.onNode(hasText("戻る") and hasAnyAncestor(isDialog())).performClick()
        composeRule.waitUntilText("クイズを始める")

        composeRule.onNodeWithText("クイズを始める").performClick()
        waitForProgress("0 / 3")

        assertExitDialogGone()
    }

    @Test
    fun afterLastAnswer_backDoesNotShowExitDialog() {
        composeRule.startQuizWithNickname("ExitFinishingTester")
        answerThroughLastQuestionSubmit()

        pressSystemBack()

        assertExitDialogGone()
        composeRule.waitUntilText("クイズ完了")
    }

    private fun assertExitDialogGone() {
        composeRule.waitUntil(timeoutMillis = 5_000) {
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

    private fun waitForProgress(label: String) {
        composeRule.waitUntilText(label)
    }

    private fun answerThroughLastQuestionSubmit() {
        waitForProgress("0 / 3")
        composeRule.clickChoice("Compose Multiplatform")
        composeRule.clickSubmitAnswer()

        waitForProgress("1 / 3")
        composeRule.clickChoice("count の変更で UI が再 Composition される")
        composeRule.clickChoice("Button の onClick はユーザー操作で呼ばれる")
        composeRule.clickChoice("Text の内容は状態に連動して更新される")
        composeRule.clickSubmitAnswer()

        waitForProgress("2 / 3")
        composeRule.clickSubmitAnswer()
        composeRule.waitForIdle()
    }
}
