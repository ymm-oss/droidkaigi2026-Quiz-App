package com.droidkaigi.quiz

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
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
        answerAllQuestionsCorrectly()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("ランキングを見る"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("ランキングを見る").performClick()
        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("ホームに戻る"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("ホームに戻る").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("クイズを始める"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("クイズを始める").performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText(firstQuestionPrompt))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("0 / 3").assertExists()
    }

    @Test
    fun fullQuizFlow_reachesRankingWithNickname() {
        composeRule.onNodeWithText("ニックネーム").performTextInput("FlowTester")
        composeRule.onNodeWithText("クイズを始める").performClick()
        answerAllQuestionsCorrectly()

        composeRule.waitUntil(timeoutMillis = 15_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("ランキングを見る"))
                .fetchSemanticsNodes().isNotEmpty()
        }
        composeRule.onNodeWithText("ランキングを見る").performClick()

        composeRule.waitUntil(timeoutMillis = 5_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText("FlowTester"))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForProgress(label: String) {
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(androidx.compose.ui.test.hasText(label))
                .fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun answerAllQuestionsCorrectly() {
        waitForProgress("0 / 3")
        composeRule.onNodeWithText("Compose Multiplatform").performClick()
        composeRule.onNodeWithText("回答する").performClick()

        waitForProgress("1 / 3")
        composeRule.onNodeWithText("宣言的 UI").performClick()
        composeRule.onNodeWithText("プレビュー可能").performClick()
        composeRule.onNodeWithText("状態駆動").performClick()
        composeRule.onNodeWithText("回答する").performClick()

        waitForProgress("2 / 3")
        composeRule.onNodeWithText("回答する").performClick()
        composeRule.waitForIdle()
    }
}
