package jp.co.yumemi.quiz.droidkaigi.feature.quiz.result

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
 * JVM Compose UI smoke: Result content renders the accuracy without instrumentation.
 */
class ResultContentJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun resultContent_showsAccuracyWithSinglePercentSign() = runComposeUiTest {
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        ResultContent(
                            nickname = "Compose 太郎",
                            correctCount = 1,
                            totalCount = 6,
                            score = 22,
                            onGoToRankingClick = {},
                            animateScore = false,
                        )
                    }
                }
            }
        }

        // Compose Multiplatform resources do not unescape "%%", so the raw string must contain a
        // single "%".
        onNodeWithText("22%").assertIsDisplayed()
        onNodeWithText("完全正解 1 / 6 問").assertIsDisplayed()
    }
}
