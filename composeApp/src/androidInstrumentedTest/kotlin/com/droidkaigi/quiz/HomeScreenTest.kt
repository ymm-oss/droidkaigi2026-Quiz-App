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
class HomeScreenTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun home_showsTitleAndNicknameField() {
        composeRule.onNodeWithText("DroidKaigi 2026 Quiz").assertExists()
        composeRule.onNodeWithText("ニックネーム").assertExists()
    }

    @Test
    fun startQuiz_navigatesToFirstQuestion() {
        composeRule.onNodeWithText("ニックネーム").performTextInput("TestPlayer")
        composeRule.onNodeWithText("クイズを始める").performClick()
        composeRule.waitUntil(timeoutMillis = 10_000) {
            composeRule.onAllNodes(
                androidx.compose.ui.test.hasText("Kotlin Multiplatform で UI を共有する Jetpack ライブラリは？"),
            ).fetchSemanticsNodes().isNotEmpty()
        }
    }
}
