package com.droidkaigi.quiz

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    private val androidComposeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(ForceJapaneseLocaleRule()).around(androidComposeRule)

    @Test
    fun home_showsTitleAndNicknameField() {
        androidComposeRule.waitUntilText("DroidKaigi 2026 Quiz")
        androidComposeRule.onNodeWithText("DroidKaigi 2026 Quiz").assertExists()
        androidComposeRule.onNodeWithText("ニックネーム").assertExists()
    }

    @Test
    fun startQuiz_navigatesToFirstQuestion() {
        androidComposeRule.startQuizWithNickname("TestPlayer")
        androidComposeRule.onNodeWithText("1 / 3").assertExists()
        androidComposeRule.waitUntilText("共通化できる Jetpack ライブラリはどれ？", substring = true)
    }
}
