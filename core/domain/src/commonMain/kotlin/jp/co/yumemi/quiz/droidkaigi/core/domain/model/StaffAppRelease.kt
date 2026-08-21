package jp.co.yumemi.quiz.droidkaigi.core.domain.model

/**
 * Latest staff Desktop release metadata (Firestore `staffAppRelease/latest`).
 * [storagePath] is a Storage object path (not a public download URL).
 */
data class StaffAppRelease(
    val version: String,
    val versionCode: Int,
    val storagePath: String,
    val sha256: String,
    val releaseNotes: String = "",
    val publishedAtEpochMillis: Long? = null,
)

data class LocalStaffAppVersion(
    val version: String,
    val versionCode: Int,
)

sealed interface StaffAppUpdateStatus {
    data object UpToDate : StaffAppUpdateStatus
    data object Unavailable : StaffAppUpdateStatus
    data class UpdateAvailable(val release: StaffAppRelease) : StaffAppUpdateStatus
}
