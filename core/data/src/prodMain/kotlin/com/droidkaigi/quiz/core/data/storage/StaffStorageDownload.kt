package com.droidkaigi.quiz.core.data.storage

/**
 * Platform helpers for authenticated Firebase Storage downloads (staff Desktop DMG).
 */
internal expect fun firebaseStorageBucket(): String

internal expect suspend fun downloadAuthenticatedStorageObject(
    storagePath: String,
    idToken: String,
    destinationPath: String,
    onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit,
): Result<String>

internal expect fun openLocalFile(path: String)

internal expect fun deleteLocalFile(path: String)

internal expect fun defaultStaffDmgDownloadPath(version: String): String

internal expect suspend fun sha256HexOfFile(path: String): String
