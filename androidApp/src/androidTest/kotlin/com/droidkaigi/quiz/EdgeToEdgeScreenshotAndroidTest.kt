package com.droidkaigi.quiz

import android.graphics.Bitmap
import android.graphics.Color
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.ReorderItem
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.ui.locale.AppLocalePreference
import com.droidkaigi.quiz.core.ui.locale.LocalAppLocale
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.feature.quiz.home.HomeContent
import com.droidkaigi.quiz.feature.quiz.quiz.QuizContent
import com.droidkaigi.quiz.feature.quiz.quiz.QuizUiState
import com.droidkaigi.quiz.feature.quiz.result.ResultContent
import com.droidkaigi.quiz.feature.ranking.RankingContent
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Captures Android screenshots for the edge-to-edge insets work.
 *
 * Renders screen `*Content` composables with fixed state — no navigation or taps.
 * See `.cursor/skills/android-compose-screenshot/SKILL.md`.
 */
@RunWith(AndroidJUnit4::class)
class EdgeToEdgeScreenshotAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Before
    fun enableEdgeToEdgeOnHost() {
        composeRule.runOnUiThread {
            composeRule.activity.enableEdgeToEdge(
                statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
                navigationBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            )
        }
    }

    private fun outputDir(): File {
        val fromAgp = InstrumentationRegistry.getArguments().getString("additionalTestOutputDir")
        val dir = if (fromAgp != null) {
            File(fromAgp)
        } else {
            InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null)
                ?: error("No external files dir")
        }
        dir.mkdirs()
        return dir
    }

    private fun capture(fileName: String) {
        val bitmap = composeRule.onRoot().captureToImage().asAndroidBitmap()
        FileOutputStream(File(outputDir(), fileName)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    @Test
    fun captureHomeEdgeToEdge() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        HomeContent(
                            nickname = "EdgeTester",
                            isLoading = false,
                            errorMessage = null,
                            onNicknameChange = {},
                            onStartClick = {},
                            localePreference = AppLocalePreference.Japanese,
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        capture("android-01-edge-home.png")
    }

    @Test
    fun captureQuizReorderEdgeToEdge() {
        val question = Reorder(
            id = "screenshot-reorder",
            prompt = "ヒント: Activity.onCreate のなかで setContent { } を呼び、その後 Composition が走ってからレイアウト & 描画されます。",
            items = listOf(
                ReorderItem("a", "Activity.onCreate"),
                ReorderItem("b", "setContent { }"),
                ReorderItem("c", "Composition"),
                ReorderItem("d", "レイアウト & 描画"),
            ),
            correctOrder = listOf("a", "b", "c", "d"),
        )
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = QuizUiState(
                                prompt = question.prompt,
                                progress = "3 / 3",
                                progressFraction = 1f,
                                question = question,
                                reorderIds = listOf("a", "b", "c", "d"),
                                canSubmit = true,
                            ),
                            onSelectSingle = {},
                            onToggleMultiple = {},
                            onMoveReorder = { _, _ -> },
                            onSubmitAnswer = {},
                            onContinueAfterFeedback = {},
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        capture("android-02-edge-quiz-reorder.png")
    }

    @Test
    fun captureQuizChoiceEdgeToEdge() {
        val question = SingleChoice(
            id = "screenshot-single",
            prompt = "UI を Android / Desktop / Web などで共通化できる Jetpack ライブラリはどれ？",
            options = listOf(
                ChoiceOption("a", "Jetpack XML"),
                ChoiceOption("b", "Compose Multiplatform"),
                ChoiceOption("c", "Flutter"),
            ),
            correctId = "b",
        )
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = QuizUiState(
                                prompt = question.prompt,
                                progress = "1 / 3",
                                progressFraction = 0.33f,
                                question = question,
                                selectedSingleId = "b",
                                canSubmit = true,
                            ),
                            onSelectSingle = {},
                            onToggleMultiple = {},
                            onMoveReorder = { _, _ -> },
                            onSubmitAnswer = {},
                            onContinueAfterFeedback = {},
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        capture("android-03-edge-quiz-choice.png")
    }

    @Test
    fun captureResultEdgeToEdge() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        ResultContent(
                            nickname = "EdgeTester",
                            correctCount = 2,
                            totalCount = 3,
                            targetScore = 1800,
                            onGoToRankingClick = {},
                            animateScore = false,
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        capture("android-04-edge-result.png")
    }

    @Test
    fun captureRankingEdgeToEdge() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        RankingContent(
                            entries = listOf(
                                RankingEntry("EdgeTester", 1800, 1_700_000_000_000L),
                                RankingEntry("Alice", 1500, 1_700_000_100_000L),
                                RankingEntry("Bob", 900, 1_700_000_200_000L),
                            ),
                            highlightNickname = "EdgeTester",
                            isLoading = false,
                            onGoHomeClick = {},
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        capture("android-05-edge-ranking.png")
    }
}
