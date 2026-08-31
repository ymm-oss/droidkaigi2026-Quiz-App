package jp.co.yumemi.quiz.droidkaigi.feature.ranking

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocale
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
import kotlin.test.Test

/**
 * Captures phone-sized Ranking screenshot for the VitePress docs site.
 * Output: docs/screenshots/android/
 */
class RankingScreenshotJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureRanking() = runDesktopComposeUiTest(width = 1080, height = 1920) {
        val entries = listOf(
            RankingEntry("Compose 太郎", 100, 1_780_000_000_000L, totalCount = 3),
            RankingEntry("Kotlin 花子", 83, 1_780_000_100_000L, totalCount = 3),
            RankingEntry("Metro 次郎", 72, 1_780_000_200_000L, totalCount = 3),
            RankingEntry("Nav3 三郎", 50, 1_780_000_300_000L, totalCount = 3),
            RankingEntry("Wasm 四郎", 33, 1_780_000_400_000L, totalCount = 3),
        )
        setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        RankingContent(
                            entries = entries,
                            highlightNickname = "Compose 太郎",
                            publishedFolders = listOf(
                                jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder(
                                    id = "easy",
                                    name = "一般向け",
                                    description = "会場向け初級",
                                    sortOrder = 0,
                                ),
                                jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder(
                                    id = "hard",
                                    name = "高難易度",
                                    description = "上級者向け",
                                    sortOrder = 1,
                                ),
                            ),
                            selectedFolderId = "easy",
                            isLoading = false,
                            onGoHomeClick = {},
                        )
                    }
                }
            }
        }
        onNodeWithText("今日のランキング").assertIsDisplayed()
        onNodeWithText("Compose 太郎").assertIsDisplayed()
        captureAndroidSurfacePng("android-ranking.png")
    }
}

private fun androidScreenshotOutputDir(): File {
    val fromProp = System.getProperty("android.screenshot.dir")
    if (!fromProp.isNullOrBlank()) {
        return File(fromProp).also { it.mkdirs() }
    }
    var dir: File? = File(System.getProperty("user.dir"))
    while (dir != null) {
        if (File(dir, "settings.gradle.kts").exists() || File(dir, "docs").isDirectory) {
            return File(dir, "docs/screenshots/android").also { it.mkdirs() }
        }
        dir = dir.parentFile
    }
    return File("docs/screenshots/android").also { it.mkdirs() }
}

private fun ImageBitmap.writePng(file: File) {
    file.parentFile?.mkdirs()
    val data = Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)
        ?: error("Failed to encode PNG: ${file.name}")
    file.writeBytes(data.bytes)
}

@OptIn(ExperimentalTestApi::class)
private fun DesktopComposeUiTest.captureAndroidSurfacePng(fileName: String): File {
    val file = File(androidScreenshotOutputDir(), fileName)
    captureToImage().writePng(file)
    return file
}
