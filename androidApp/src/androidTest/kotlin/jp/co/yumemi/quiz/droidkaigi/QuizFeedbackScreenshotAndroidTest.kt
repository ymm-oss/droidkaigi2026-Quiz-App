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
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ChoiceOption
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocale
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz.QuizContent
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz.QuizUiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.File
import java.io.FileOutputStream

/**
 * Captures Android screenshots of the quiz answer-feedback overlay for PR review.
 *
 * Renders [QuizContent] directly with fixed state — no navigation, IME or taps — so the
 * capture never depends on emulator dialogs, gesture areas or scroll position.
 *
 * See `.cursor/skills/android-compose-screenshot/SKILL.md` for how to run and collect output.
 */
@RunWith(AndroidJUnit4::class)
class QuizFeedbackScreenshotAndroidTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val question = SingleChoice(
        id = "screenshot-single",
        prompt = "UI を Android / Desktop / Web などで共通化できる Jetpack ライブラリはどれ？",
        explanationMarkdown = """
            **Compose Multiplatform** は、Compose の宣言的 UI を Android だけでなく
            Desktop・iOS・Web でも共有できる仕組みです。

            Jetpack XML は Android の View システム、Flutter は別の UI フレームワークです。
        """.trimIndent(),
        options = listOf(
            ChoiceOption("a", "Jetpack XML"),
            ChoiceOption("b", "Compose Multiplatform"),
            ChoiceOption("c", "Flutter"),
        ),
        correctId = "b",
    )

    private fun state(correct: Boolean, finishing: Boolean = false) = QuizUiState(
        prompt = question.prompt,
        progress = "1 / 3",
        progressFraction = 0.33f,
        question = question,
        selectedSingleId = if (correct) "b" else "a",
        canSubmit = true,
        showFeedback = true,
        lastAnswerCorrect = correct,
        isFinishing = finishing,
    )

    /**
     * AGP passes `additionalTestOutputDir` to the runner and pulls its contents into
     * `androidApp/build/outputs/connected_android_test_additional_output/` after the run.
     * That directory is readable by the adb shell user, unlike `/sdcard/Android/data`.
     */
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

    private fun render(state: QuizUiState) {
        composeRule.setContent {
            CompositionLocalProvider(LocalAppLocale provides "ja") {
                key("ja") {
                    QuizTheme {
                        QuizContent(
                            state = state,
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
    }

    @Test
    fun captureCorrectFeedbackOverlay() {
        render(state(correct = true))
        capture("android-01-feedback-correct.png")
    }

    @Test
    fun captureIncorrectFeedbackOverlay() {
        render(state(correct = false))
        capture("android-02-feedback-incorrect.png")
    }

    @Test
    fun captureFinishingFeedbackOverlay() {
        render(state(correct = true, finishing = true))
        capture("android-03-feedback-finish.png")
    }
}
