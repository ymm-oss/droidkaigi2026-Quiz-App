package jp.co.yumemi.quiz.droidkaigi.feature.quiz.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocale
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import kotlin.test.Test

/**
 * JVM Compose UI smoke: Home content renders without Android instrumentation.
 */
class HomeContentJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun homeContent_showsTitleAndStartButton() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
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
            }
        }

        onNodeWithText("DroidKaigi 2026 Quiz").assertIsDisplayed()
        onNodeWithText("クイズを始める").assertIsDisplayed()
    }
}
