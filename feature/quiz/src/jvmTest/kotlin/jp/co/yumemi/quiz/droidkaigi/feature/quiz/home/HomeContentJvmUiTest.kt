package jp.co.yumemi.quiz.droidkaigi.feature.quiz.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
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
                            publishedFolders = listOf(
                                QuizFolder(id = "easy", name = "一般向け", description = "会場向け初級"),
                                QuizFolder(id = "hard", name = "高難易度", description = "上級者向け"),
                            ),
                            selectedFolderId = "easy",
                            onNicknameChange = {},
                            onStartClick = {},
                        )
                    }
                }
            }
        }

        onNodeWithText("DroidKaigi 2026 Quiz").assertIsDisplayed()
        onNodeWithText("クイズを始める").assertIsDisplayed()
        onNodeWithText("一般向け").assertIsDisplayed()
    }
}
