package com.droidkaigi.quiz

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class RankingAndroidTest {
    private val androidComposeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(ForceJapaneseLocaleRule()).around(androidComposeRule)

    @Test
    fun rankingTab_showsTodayTitle() {
        androidComposeRule.onNodeWithText("ランキング").performClick()
        androidComposeRule.onNodeWithText("今日のランキング").assertExists()
    }
}
