package jp.co.yumemi.quiz.droidkaigi.feature.quiz

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocale
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.home.HomeContent
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.result.ResultContent
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
                            publishedFolders = sampleHomeFolders,
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

private val sampleHomeFolders = listOf(
    QuizFolder(id = "easy", name = "一般向け", description = "会場向け初級", sortOrder = 0),
    QuizFolder(id = "hard", name = "高難易度", description = "上級者向け", sortOrder = 1),
)
