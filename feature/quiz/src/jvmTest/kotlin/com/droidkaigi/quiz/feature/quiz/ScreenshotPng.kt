package com.droidkaigi.quiz.feature.quiz

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File

internal fun quizScreenshotOutputDir(): File = screenshotOutputDir(
    propertyName = "quiz.screenshot.dir",
    relativePath = "docs/screenshots/quiz",
)

internal fun androidScreenshotOutputDir(): File = screenshotOutputDir(
    propertyName = "android.screenshot.dir",
    relativePath = "docs/screenshots/android",
)

private fun screenshotOutputDir(propertyName: String, relativePath: String): File {
    val fromProp = System.getProperty(propertyName)
    if (!fromProp.isNullOrBlank()) {
        return File(fromProp).also { it.mkdirs() }
    }
    var dir: File? = File(System.getProperty("user.dir"))
    while (dir != null) {
        if (File(dir, "settings.gradle.kts").exists() || File(dir, "docs").isDirectory) {
            return File(dir, relativePath).also { it.mkdirs() }
        }
        dir = dir.parentFile
    }
    return File(relativePath).also { it.mkdirs() }
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

@OptIn(ExperimentalTestApi::class)
internal fun DesktopComposeUiTest.captureAndroidSurfacePng(fileName: String): File {
    val file = File(androidScreenshotOutputDir(), fileName)
    captureToImage().writePng(file)
    return file
}
