package com.droidkaigi.quiz

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class QuizFlowAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val firstQuestionSnippet = "共通化できる Jetpack ライブラリはどれ？"

    @Test
    fun restartQuiz_afterCompletion_showsFirstQuestionFromBeginning() {
        composeRule.startQuizWithNickname("RestartTester")
        answerQuizThroughResult()

        composeRule.waitUntilText("ランキングを見る")
        composeRule.onNodeWithText("ランキングを見る").performClick()
        composeRule.waitUntilText("ホームに戻る")
        composeRule.onNodeWithText("ホームに戻る").performClick()

        composeRule.waitUntilText("クイズを始める")
        composeRule.onNodeWithText("クイズを始める").performClick()
        composeRule.waitUntilText("0 / 3")

        composeRule.waitUntilText(firstQuestionSnippet, substring = true)
        composeRule.onNodeWithText("0 / 3").assertExists()
    }

    @Test
    fun fullQuizFlow_reachesRankingWithNickname() {
        composeRule.startQuizWithNickname("FlowTester")
        answerQuizThroughResult()

        composeRule.waitUntilText("ランキングを見る")
        composeRule.onNodeWithText("ランキングを見る").performClick()

        composeRule.waitUntilText("今日のランキング")
    }

    private fun waitForProgress(label: String) {
        composeRule.waitUntilText(label)
    }

    /**
     * 単一・複数選択は正解で回答。並び替えは UI 操作（ドラッグ）をテストしない。
     */
    private fun answerQuizThroughResult() {
        waitForProgress("0 / 3")
        composeRule.clickChoice("Compose Multiplatform")
        composeRule.clickSubmitAnswer()
        composeRule.waitForAnswerFeedback()

        waitForProgress("1 / 3")
        composeRule.clickChoice("count の変更で UI が再 Composition される")
        composeRule.clickChoice("Button の onClick はユーザー操作で呼ばれる")
        composeRule.clickChoice("Text の内容は状態に連動して更新される")
        composeRule.clickSubmitAnswer()

        waitForProgress("2 / 3")
        composeRule.clickSubmitAnswer()
        composeRule.waitUntilText("クイズ完了")
    }
}
