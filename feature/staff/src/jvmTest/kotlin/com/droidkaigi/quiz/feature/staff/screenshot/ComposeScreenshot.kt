package com.droidkaigi.quiz.feature.staff.screenshot

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File

/**
 * Saves a Compose [ImageBitmap] (e.g. from [androidx.compose.ui.test.DesktopComposeUiTest.captureToImage])
 * as PNG under [screenshotDir].
 */
fun ImageBitmap.saveAsPng(fileName: String): File {
    val dir = screenshotDir()
    dir.mkdirs()
    val file = File(dir, fileName)
    Image.makeFromBitmap(asSkiaBitmap()).use { skiaImage ->
        val data = skiaImage.encodeToData(EncodedImageFormat.PNG)
            ?: error("Failed to encode PNG: $fileName")
        file.writeBytes(data.bytes)
    }
    check(file.isFile && file.length() > 0L) { "Screenshot was not written: ${file.absolutePath}" }
    return file
}

/**
 * Override with `-Dquiz.screenshot.dir=...` or `QUIZ_SCREENSHOT_DIR`.
 * Defaults to `<module>/build/screenshots`.
 */
fun screenshotDir(): File {
    val fromProperty = System.getProperty("quiz.screenshot.dir")?.takeIf { it.isNotBlank() }
    val fromEnv = System.getenv("QUIZ_SCREENSHOT_DIR")?.takeIf { it.isNotBlank() }
    return File(fromProperty ?: fromEnv ?: "build/screenshots")
}
