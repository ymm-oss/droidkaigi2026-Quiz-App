package com.droidkaigi.quiz.core.data.storage

import com.droidkaigi.quiz.core.data.firestore.GoogleServicesLoader
import java.awt.Desktop
import java.io.File
import java.io.FileInputStream
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import kotlin.coroutines.cancellation.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

internal actual fun firebaseStorageBucket(): String {
    val config = GoogleServicesLoader.load()
    return config.storageBucket
        ?: "${config.projectId}.appspot.com"
}

internal actual suspend fun downloadAuthenticatedStorageObject(
    storagePath: String,
    idToken: String,
    destinationPath: String,
    onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit,
): Result<String> = withContext(Dispatchers.IO) {
    runCatching {
        val bucket = firebaseStorageBucket()
        val encodedPath = URLEncoder.encode(storagePath, StandardCharsets.UTF_8)
            .replace("+", "%20")
        val url = URI(
            "https://firebasestorage.googleapis.com/v0/b/$bucket/o/$encodedPath?alt=media",
        ).toURL()
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "GET"
            setRequestProperty("Authorization", "Firebase $idToken")
            connectTimeout = 30_000
            readTimeout = 120_000
        }
        try {
            val code = connection.responseCode
            if (code !in 200..299) {
                val errorBody = connection.errorStream?.bufferedReader()?.readText().orEmpty()
                error("Storage download failed HTTP $code: $errorBody")
            }
            val total = connection.contentLengthLong.takeIf { it >= 0 }
            val destination = File(destinationPath)
            destination.parentFile?.mkdirs()
            connection.inputStream.use { input ->
                destination.outputStream().use { output ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var readTotal = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        output.write(buffer, 0, read)
                        readTotal += read
                        onProgress(readTotal, total)
                    }
                }
            }
            destination.absolutePath
        } catch (e: CancellationException) {
            throw e
        } finally {
            connection.disconnect()
        }
    }
}

internal actual fun openLocalFile(path: String) {
    val file = File(path)
    if (!file.isFile) error("File not found: $path")
    if (!Desktop.isDesktopSupported() || !Desktop.getDesktop().isSupported(Desktop.Action.OPEN)) {
        error("Opening files is not supported on this desktop")
    }
    Desktop.getDesktop().open(file)
}

internal actual fun defaultStaffDmgDownloadPath(version: String): String {
    val downloads = File(System.getProperty("user.home"), "Downloads")
    return File(downloads, "droidkaigi-quiz-staff-$version.dmg").absolutePath
}

internal actual fun sha256HexOfFile(path: String): String {
    val digest = MessageDigest.getInstance("SHA-256")
    FileInputStream(File(path)).use { input ->
        val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
        while (true) {
            val read = input.read(buffer)
            if (read < 0) break
            digest.update(buffer, 0, read)
        }
    }
    return digest.digest().joinToString("") { byte -> "%02x".format(byte) }
}
