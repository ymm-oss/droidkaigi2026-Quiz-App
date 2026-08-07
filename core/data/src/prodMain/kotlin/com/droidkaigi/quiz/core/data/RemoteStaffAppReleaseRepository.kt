package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.auth.staffCurrentIdToken
import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.data.firestore.FirestoreService
import com.droidkaigi.quiz.core.data.firestore.StaffAppReleaseFirestoreDocument
import com.droidkaigi.quiz.core.data.storage.defaultStaffDmgDownloadPath
import com.droidkaigi.quiz.core.data.storage.downloadAuthenticatedStorageObject
import com.droidkaigi.quiz.core.data.storage.openLocalFile
import com.droidkaigi.quiz.core.data.storage.sha256HexOfFile
import com.droidkaigi.quiz.core.domain.model.StaffAppRelease
import com.droidkaigi.quiz.core.domain.repository.StaffAppReleaseRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class RemoteStaffAppReleaseRepository(
    private val firestore: FirestoreService,
    private val staffAuthHolder: StaffAuthHolder,
) : StaffAppReleaseRepository {
    override suspend fun fetchLatestRelease(): StaffAppRelease? =
        firestore.getStaffAppRelease()?.toDomain()

    override suspend fun downloadDmg(
        release: StaffAppRelease,
        onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit,
    ): Result<String> {
        val idToken = resolveIdToken()
            ?: return Result.failure(IllegalStateException("スタッフ認証トークンがありません。再ログインしてください。"))
        val destination = defaultStaffDmgDownloadPath(release.version)
        val downloaded = downloadAuthenticatedStorageObject(
            storagePath = release.storagePath,
            idToken = idToken,
            destinationPath = destination,
            onProgress = onProgress,
        ).getOrElse { return Result.failure(it) }

        val actualSha = runCatching { sha256HexOfFile(downloaded) }.getOrElse {
            return Result.failure(it)
        }
        if (!actualSha.equals(release.sha256, ignoreCase = true)) {
            return Result.failure(
                IllegalStateException(
                    "ダウンロードした DMG のチェックサムが一致しません（expected=${release.sha256}, actual=$actualSha）",
                ),
            )
        }
        return Result.success(downloaded)
    }

    override fun openDownloadedFile(path: String) {
        openLocalFile(path)
    }

    private suspend fun resolveIdToken(): String? {
        try {
            val refreshed = staffCurrentIdToken(forceRefresh = false)
                ?: staffCurrentIdToken(forceRefresh = true)
            if (refreshed != null) {
                staffAuthHolder.firebaseIdToken = refreshed
                return refreshed
            }
        } catch (e: kotlin.coroutines.cancellation.CancellationException) {
            throw e
        } catch (_: Exception) {
            // Fall back to the cached token from sign-in when refresh is unavailable.
        }
        return staffAuthHolder.firebaseIdToken
    }
}

private fun StaffAppReleaseFirestoreDocument.toDomain(): StaffAppRelease =
    StaffAppRelease(
        version = version,
        versionCode = versionCode,
        storagePath = storagePath,
        sha256 = sha256,
        releaseNotes = releaseNotes,
        publishedAtEpochMillis = publishedAtEpochMillis,
    )
