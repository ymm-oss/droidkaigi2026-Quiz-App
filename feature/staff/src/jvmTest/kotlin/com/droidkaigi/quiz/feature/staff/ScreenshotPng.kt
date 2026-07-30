package com.droidkaigi.quiz.feature.staff

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.test.DesktopComposeUiTest
import androidx.compose.ui.test.ExperimentalTestApi
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import java.io.File

internal fun screenshotOutputDir(): File {
    val fromProp = System.getProperty("staff.screenshot.dir")
    if (!fromProp.isNullOrBlank()) {
        return File(fromProp).also { it.mkdirs() }
    }
    var dir: File? = File(System.getProperty("user.dir"))
    while (dir != null) {
        if (File(dir, "settings.gradle.kts").exists() || File(dir, "docs").isDirectory) {
            return File(dir, "docs/screenshots/staff").also { it.mkdirs() }
        }
        dir = dir.parentFile
    }
    return File("docs/screenshots/staff").also { it.mkdirs() }
}

internal fun ImageBitmap.writePng(file: File) {
    file.parentFile?.mkdirs()
    val data = Image.makeFromBitmap(asSkiaBitmap()).encodeToData(EncodedImageFormat.PNG)
        ?: error("Failed to encode PNG: ${file.name}")
    file.writeBytes(data.bytes)
}

/** Surface capture (includes dialogs; avoids multi-root onRoot() failures). */
@OptIn(ExperimentalTestApi::class)
internal fun DesktopComposeUiTest.captureSurfacePng(fileName: String): File {
    val file = File(screenshotOutputDir(), fileName)
    captureToImage().writePng(file)
    return file
}
