package jp.co.yumemi.quiz.droidkaigi

import android.graphics.Bitmap
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.key
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
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
 * キーボード表示中の Home（ニックネーム入力）のスクショ。
 * `imePadding` によりコンテンツがキーボード上に収まり、「クイズを始める」に
 * スクロールで到達・タップできることを full-screen キャプチャ（IME 込み）で示す。
 *
 * 実行前に `adb shell wm size 1080x1920`（Issue #77 の再現サイズ）を推奨。
 */
@RunWith(AndroidJUnit4::class)
class HomeImeScreenshotAndroidTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

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

    /** IME を含めた実画面全体を撮る（Compose ルートのキャプチャには IME が写らないため）。 */
    private fun captureFullScreen(fileName: String) {
        val bitmap = InstrumentationRegistry.getInstrumentation().uiAutomation.takeScreenshot()
            ?: error("takeScreenshot returned null")
        FileOutputStream(File(outputDir(), fileName)).use { out ->
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
        }
    }

    private fun renderHome() {
        composeRule.activityRule.scenario.onActivity { it.enableEdgeToEdge() }
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
                        )
                    }
                }
            }
        }
        composeRule.waitForIdle()
    }

    @Test
    fun captureHomeWithImeOpen() {
        renderHome()
        composeRule.onNode(hasSetTextAction()).performClick()
        composeRule.waitForIdle()
        // IME の表示アニメーション完了を待つ（インセット反映込み）
        Thread.sleep(1_500)
        composeRule.waitForIdle()
        captureFullScreen("android-home-ime-focused.png")
    }

    @Test
    fun captureHomeWithImeOpen_scrolledToStartButton() {
        renderHome()
        composeRule.onNode(hasSetTextAction()).performClick()
        composeRule.waitForIdle()
        Thread.sleep(1_500)
        composeRule.onNodeWithText("クイズを始める").performScrollTo()
        composeRule.waitForIdle()
        Thread.sleep(500)
        captureFullScreen("android-home-ime-start-visible.png")
    }
}
