package jp.co.yumemi.quiz.droidkaigi.staff.preview

import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizStaffTheme
import jp.co.yumemi.quiz.droidkaigi.di.initStaffQuizAppGraph
import kotlinx.coroutines.delay
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
        onNodeWithText("393 dp").assertIsDisplayed()
        for (attempt in 0 until 100) {
            val quizLoaded = runCatching {
                onNodeWithText("Compose Multiplatform").assertIsDisplayed()
                true
            }.getOrDefault(false)
            if (quizLoaded) break
            check(attempt < 99) { "Quiz preview did not load in time" }
            delay(100)
        }

        captureSurfacePng("staff-participant-preview.png")
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
