package jp.co.yumemi.quiz.droidkaigi.core.data.storage

internal actual fun firebaseStorageBucket(): String =
    error("Staff DMG Storage download is not supported on Android")

internal actual suspend fun downloadAuthenticatedStorageObject(
    storagePath: String,
    idToken: String,
    destinationPath: String,
    onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit,
): Result<String> = Result.failure(
    UnsupportedOperationException("Staff DMG Storage download is not supported on Android"),
)

internal actual fun openLocalFile(path: String) {
    error("Opening downloaded staff DMG is not supported on Android")
}

internal actual fun deleteLocalFile(path: String) = Unit

internal actual fun defaultStaffDmgDownloadPath(version: String): String =
    error("Staff DMG download path is not supported on Android")

internal actual suspend fun sha256HexOfFile(path: String): String =
    error("Staff DMG checksum is not supported on Android")
