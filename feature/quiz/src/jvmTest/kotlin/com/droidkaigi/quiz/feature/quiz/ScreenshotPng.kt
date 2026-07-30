package com.droidkaigi.quiz.feature.quiz

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File

internal fun quizScreenshotOutputDir(): File {
    val fromProp = System.getProperty("quiz.screenshot.dir")
    if (!fromProp.isNullOrBlank()) {
        return File(fromProp).also { it.mkdirs() }
    }
    var dir: File? = File(System.getProperty("user.dir"))
    while (dir != null) {
        if (File(dir, "settings.gradle.kts").exists() || File(dir, "docs").isDirectory) {
            return File(dir, "docs/screenshots/quiz").also { it.mkdirs() }
        }
        dir = dir.parentFile
    }
    return File("docs/screenshots/quiz").also { it.mkdirs() }
}

internal fun ImageBitmap.writePng(file: File) {
    file.parentFile?.mkdirs()
    val data = Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)
        ?: error("Failed to encode PNG: ${file.name}")
    file.writeBytes(data.bytes)
}

/** Surface capture (includes overlays; avoids multi-root onRoot() failures). */
@OptIn(ExperimentalTestApi::class)
internal fun DesktopComposeUiTest.captureSurfacePng(fileName: String): File {
    val file = File(quizScreenshotOutputDir(), fileName)
    captureToImage().writePng(file)
    return file
}
