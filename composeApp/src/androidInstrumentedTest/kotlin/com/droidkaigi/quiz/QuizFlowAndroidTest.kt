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

    @Test
    fun fullQuizFlow_reachesRankingWithNickname() {
        composeRule.onNodeWithText("ニックネーム").performTextInput("FlowTester")
        composeRule.onNodeWithText("クイズを始める").performClick()

        // Q1 single choice
        composeRule.waitForIdle()
        composeRule.onNodeWithText("Compose Multiplatform").performClick()
        composeRule.onNodeWithText("回答する").performClick()

        // Q2 multiple choice
        composeRule.waitForIdle()
        composeRule.onNodeWithText("宣言的 UI").performClick()
        composeRule.onNodeWithText("プレビュー可能").performClick()
        composeRule.onNodeWithText("状態駆動").performClick()
        composeRule.onNodeWithText("回答する").performClick()

        // Q3 reorder — default order is correct
        composeRule.waitForIdle()
        composeRule.onNodeWithText("回答する").performClick()

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
}
