package com.droidkaigi.quiz

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
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
        composeRule.waitUntilText("DroidKaigi 2026 Quiz")
        composeRule.onNodeWithText("DroidKaigi 2026 Quiz").assertExists()
        composeRule.onNodeWithText("ニックネーム").assertExists()
    }

    @Test
    fun startQuiz_navigatesToFirstQuestion() {
        composeRule.startQuizWithNickname("TestPlayer")
        composeRule.waitUntilText("Kotlin Multiplatform で UI を共有する Jetpack ライブラリは？")
    }
}
