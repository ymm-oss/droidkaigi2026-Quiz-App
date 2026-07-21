package com.droidkaigi.quiz.feature.quiz.home

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import kotlin.test.Test

/**
 * JVM Compose UI smoke: Home content renders without Android instrumentation.
 */
class HomeContentJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun homeContent_showsTitleAndStartButton() = runComposeUiTest {
        setContent {
            QuizTheme {
                HomeContent(
                    nickname = "",
                    isLoading = false,
                    errorMessage = null,
                    onNicknameChange = {},
                    onStartClick = {},
                )
            }
        }

        onNodeWithText("DroidKaigi 2026 Quiz").assertIsDisplayed()
        onNodeWithText("クイズを始める").assertIsDisplayed()
    }
}
