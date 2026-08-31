package jp.co.yumemi.quiz.droidkaigi.feature.quiz.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
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
                                QuizFolder(
                                    id = "easy",
                                    name = "Day 1（運営用）",
                                    description = "運営メモ",
                                    publicName = "一般向け",
                                    publicDescription = "会場向け初級",
                                ),
                                QuizFolder(
                                    id = "hard",
                                    name = "高難易度",
                                    description = "上級者向け",
                                    useInternalAsPublic = true,
                                ),
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
        onNodeWithText("会場向け初級").assertIsDisplayed()
        onAllNodesWithText("Day 1（運営用）").assertCountEquals(0)
        onAllNodesWithText("運営メモ").assertCountEquals(0)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun homeContent_folderLoadFailed_showsRetry() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        HomeContent(
                            nickname = "",
                            isLoading = false,
                            errorMessage = "公開フォルダの取得に失敗しました",
                            publishedFolders = null,
                            onNicknameChange = {},
                            onStartClick = {},
                        )
                    }
                }
            }
        }

        onNodeWithText("再試行").assertIsDisplayed()
    }
}
