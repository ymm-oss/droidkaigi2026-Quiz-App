package com.droidkaigi.quiz.feature.staff.update

import com.droidkaigi.quiz.core.domain.model.StaffAppRelease

data class StaffAppUpdateUiState(
    val showDialog: Boolean = false,
    val release: StaffAppRelease? = null,
    val isDownloading: Boolean = false,
    val downloadBytesRead: Long = 0L,
    val downloadTotalBytes: Long? = null,
    val downloadedPath: String? = null,
    val errorMessage: String? = null,
) {
    val downloadProgress: Float?
        get() {
            val total = downloadTotalBytes ?: return null
            if (total <= 0L) return null
            return (downloadBytesRead.toFloat() / total.toFloat()).coerceIn(0f, 1f)
        }
}

sealed interface StaffAppUpdateIntent {
    data object Dismiss : StaffAppUpdateIntent
    data object Download : StaffAppUpdateIntent
}
