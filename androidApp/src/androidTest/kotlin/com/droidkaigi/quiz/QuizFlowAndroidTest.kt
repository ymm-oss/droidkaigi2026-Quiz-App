package com.droidkaigi.quiz

import androidx.compose.ui.test.hasText
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
class QuizFlowAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    private val firstQuestionPrompt =
        "Kotlin Multiplatform で UI を共有する Jetpack ライブラリは？"

    @Test
    fun restartQuiz_afterCompletion_showsFirstQuestionFromBeginning() {
        composeRule.onNodeWithText("ニックネーム").performTextInput("RestartTester")
        composeRule.onNodeWithText("クイズを始める").performClick()
        answerQuizThroughResult()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("ランキングを見る"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("ランキングを見る").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("ホームに戻る"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("ホームに戻る").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(hasText("クイズを始める"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("クイズを始める").performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText(firstQuestionPrompt))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("0 / 3").assertExists()
    }

    @Test
    fun fullQuizFlow_reachesRankingWithNickname() {
        composeRule.onNodeWithText("ニックネーム").performTextInput("FlowTester")
        composeRule.onNodeWithText("クイズを始める").performClick()
        answerQuizThroughResult()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("ランキングを見る"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("ランキングを見る").performClick()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("今日のランキング"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForProgress(label: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(hasText(label))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    /**
     * 単一・複数選択は正解で回答。並び替えは UI 操作（ドラッグ）をテストしない。
     */
    private fun answerQuizThroughResult() {
        waitForProgress("0 / 3")
        composeRule.onNodeWithText("Compose Multiplatform").performClick()
        composeRule.onNodeWithText("回答する").performClick()

        waitForProgress("1 / 3")
        composeRule.onNodeWithText("宣言的 UI").performClick()
        composeRule.onNodeWithText("プレビュー可能").performClick()
        composeRule.onNodeWithText("状態駆動").performClick()
        composeRule.onNodeWithText("回答する").performClick()

        waitForProgress("2 / 3")
        composeRule.onNodeWithText("回答する").performScrollTo().performClick()
        waitForResultScreen()
    }

    private fun waitForResultScreen() {
        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(hasText("クイズ完了"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }
}
