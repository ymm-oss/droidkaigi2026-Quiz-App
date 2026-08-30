package jp.co.yumemi.quiz.droidkaigi

import android.graphics.Bitmap
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocale
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.home.HomeContent
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.result.ResultContent
import jp.co.yumemi.quiz.droidkaigi.feature.ranking.RankingContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Captures Android screenshots of the participant Home / Result / Ranking screens for the docs site.
 *
 * Renders the stateless `XxxContent` composables directly with fixed state — no navigation, IME or
 * taps — so captures never depend on emulator dialogs, gesture areas or scroll position. Score and
 * confetti animations are disabled / deterministic so the output is stable.
 *
 * See `.cursor/skills/android-compose-screenshot/SKILL.md` for how to run and collect output.
 */
@RunWith(AndroidJUnit4::class)
class ParticipantScreensScreenshotAndroidTest {
    @get:Rule
    val composeRule = createComposeRule()

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
    fun captureHome() {
        composeRule.setContent {
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
        composeRule.waitForIdle()
        capture("android-home.png")
    }

    @Test
    fun captureResult() {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        ResultContent(
                            nickname = "Compose 太郎",
                            correctCount = 3,
                            totalCount = 3,
                            score = 100,
                            onGoToRankingClick = {},
                            animateScore = false,
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        capture("android-result.png")
    }

    @Test
    fun captureRanking() {
        val entries = listOf(
            RankingEntry("Compose 太郎", 100, 1_780_000_000_000L, totalCount = 3),
            RankingEntry("Kotlin 花子", 83, 1_780_000_100_000L, totalCount = 3),
            RankingEntry("Metro 次郎", 72, 1_780_000_200_000L, totalCount = 3),
            RankingEntry("Nav3 三郎", 50, 1_780_000_300_000L, totalCount = 3),
            RankingEntry("Wasm 四郎", 33, 1_780_000_400_000L, totalCount = 3),
        )
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        RankingContent(
                            entries = entries,
                            highlightNickname = "Compose 太郎",
                            publishedFolders = sampleHomeFolders,
                            selectedFolderId = "easy",
                            isLoading = false,
                            onGoHomeClick = {},
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
        capture("android-ranking.png")
    }
}

private val sampleHomeFolders = listOf(
    QuizFolder(id = "easy", name = "一般向け", description = "会場向け初級", sortOrder = 0),
    QuizFolder(id = "hard", name = "高難易度", description = "上級者向け", sortOrder = 1),
)
