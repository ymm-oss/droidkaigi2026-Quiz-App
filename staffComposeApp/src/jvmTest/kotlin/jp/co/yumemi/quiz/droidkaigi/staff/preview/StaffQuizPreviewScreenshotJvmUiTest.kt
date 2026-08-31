package jp.co.yumemi.quiz.droidkaigi.staff.preview

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.isDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runDesktopComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizStaffTheme
import jp.co.yumemi.quiz.droidkaigi.di.initStaffQuizAppGraph
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File
import kotlin.test.Test

class StaffQuizPreviewScreenshotJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureParticipantPreviewDialog() = runDesktopComposeUiTest(width = 1600, height = 1000) {
        initStaffQuizAppGraph()
        setContent {
            QuizStaffTheme {
                StaffQuizPreviewDialog(
                    folderId = "droidkaigi2026-demo",
                    onDismiss = {},
                )
            }
        }

        onNodeWithText("参加者プレビュー").assertIsDisplayed()
        onNodeWithText("プレビュー言語").assertIsDisplayed()
        onNodeWithText("393 dp").assertIsDisplayed()
        waitUntil(timeoutMillis = 10_000) {
            onNodeWithText("回答する").isDisplayed()
        }

        captureSurfacePng("staff-participant-preview.png")

        onNodeWithText("English").performClick()
        waitUntil(timeoutMillis = 10_000) {
            onNodeWithText("Submit").isDisplayed()
        }
        captureSurfacePng("staff-participant-preview-english.png")
    }
}

@OptIn(ExperimentalTestApi::class)
private fun androidx.compose.ui.test.DesktopComposeUiTest.captureSurfacePng(fileName: String) {
    val dir = System.getProperty("staff.screenshot.dir")?.let(::File)
        ?: File("docs/screenshots/staff")
    dir.mkdirs()
    val file = File(dir, fileName)
    val bitmap = captureToImage()
    val data = Image.makeFromBitmap(bitmap.asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)
        ?: error("Failed to encode PNG: $fileName")
    file.writeBytes(data.bytes)
}
