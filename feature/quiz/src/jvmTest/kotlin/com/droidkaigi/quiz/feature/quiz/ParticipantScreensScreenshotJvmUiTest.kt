package com.droidkaigi.quiz.feature.quiz

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.droidkaigi.quiz.core.ui.locale.LocalAppLocale
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.feature.quiz.home.HomeContent
import com.droidkaigi.quiz.feature.quiz.result.ResultContent
import kotlin.test.Test

/**
 * Captures phone-sized Home / Result screenshots for the VitePress docs site.
 * Output: docs/screenshots/android/
 */
class ParticipantScreensScreenshotJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureHome() = runDesktopComposeUiTest(width = 1080, height = 1920) {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        HomeContent(
                            nickname = "Compose 太郎",
                            isLoading = false,
                            errorMessage = null,
                            onNicknameChange = {},
                            onStartClick = {},
                        )
                    }
                }
            }
        }
        onNodeWithText("DroidKaigi 2026 Quiz").assertIsDisplayed()
        onNodeWithText("クイズを始める").assertIsDisplayed()
        captureAndroidSurfacePng("android-home.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureResult() = runDesktopComposeUiTest(width = 1080, height = 1920) {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        ResultContent(
                            nickname = "Compose 太郎",
                            correctCount = 3,
                            totalCount = 3,
                            targetScore = 342,
                            onGoToRankingClick = {},
                            animateScore = false,
                        )
                    }
                }
            }
        }
        onNodeWithText("クイズ完了").assertIsDisplayed()
        onNodeWithText("ランキングを見る").assertIsDisplayed()
        captureAndroidSurfacePng("android-result.png")
    }
}
