package com.droidkaigi.quiz

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RankingAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun rankingTab_showsTodayTitle() {
        composeRule.onNodeWithText("ランキング").performClick()
        composeRule.onNodeWithText("今日のランキング").assertExists()
    }
}
