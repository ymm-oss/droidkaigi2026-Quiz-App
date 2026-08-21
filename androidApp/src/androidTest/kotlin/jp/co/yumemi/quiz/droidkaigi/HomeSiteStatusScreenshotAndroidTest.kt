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
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocale
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.home.HomeContent
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Home の受付状況（sitePublished）表示のスクショ。
 * 取得失敗（エラー + 再試行）と受付前（スタッフ非公開）を区別して撮る。
 */
@RunWith(AndroidJUnit4::class)
class HomeSiteStatusScreenshotAndroidTest {
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

    private fun render(sitePublished: Boolean?, siteStatusCheckFailed: Boolean) {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        HomeContent(
                            nickname = "",
                            isLoading = false,
                            errorMessage = null,
                            onNicknameChange = {},
                            onStartClick = {},
                            sitePublished = sitePublished,
                            siteStatusCheckFailed = siteStatusCheckFailed,
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun captureSiteStatusError() {
        render(sitePublished = null, siteStatusCheckFailed = true)
        capture("android-home-site-status-error.png")
    }

    @Test
    fun captureSiteClosed() {
        render(sitePublished = false, siteStatusCheckFailed = false)
        capture("android-home-site-closed.png")
    }
}
