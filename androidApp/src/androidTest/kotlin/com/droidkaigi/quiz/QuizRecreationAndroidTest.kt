package com.droidkaigi.quiz

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.runner.RunWith

/**
 * 画面回転などの構成変更（Activity 再生成）でクイズ中に Home へ戻されないことを検証する。
 * https://github.com/ymm-oss/droidkaigi2026-Quiz-App/issues/75
 */
@RunWith(AndroidJUnit4::class)
class QuizRecreationAndroidTest {
    private val composeRule = createAndroidComposeRule<MainActivity>()

    @get:Rule
    val ruleChain: RuleChain = RuleChain.outerRule(ForceJapaneseLocaleRule()).around(composeRule)

    @Test
    fun activityRecreation_duringQuiz_staysOnQuizScreen() {
        composeRule.startQuizWithNickname("RecreateTester")
        composeRule.clickChoice("Compose Multiplatform")

        composeRule.activityRule.scenario.recreate()

        composeRule.waitUntilText("1 / 3")
        composeRule.onNodeWithText("クイズを始める").assertDoesNotExist()
    }

    @Test
    fun activityRecreation_onRanking_staysOnRanking() {
        composeRule.waitUntilText("ランキング")
        composeRule.onNodeWithText("ランキング").performClick()
        composeRule.waitUntilText("今日のランキング")

        composeRule.activityRule.scenario.recreate()

        composeRule.waitUntilText("今日のランキング")
    }
}
