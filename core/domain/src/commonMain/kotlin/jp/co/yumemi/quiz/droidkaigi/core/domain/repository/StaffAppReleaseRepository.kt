package jp.co.yumemi.quiz.droidkaigi.core.domain.repository

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppRelease

interface StaffAppReleaseRepository {
    suspend fun fetchLatestRelease(): StaffAppRelease?

    /**
     * Downloads the DMG for [release] to a local file.
     * Returns the absolute path on success.
     */
    suspend fun downloadDmg(
        release: StaffAppRelease,
        onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit = { _, _ -> },
    ): Result<String>

    /** Opens a local file with the OS default handler (e.g. mount DMG on macOS). */
    fun openDownloadedFile(path: String)
}
