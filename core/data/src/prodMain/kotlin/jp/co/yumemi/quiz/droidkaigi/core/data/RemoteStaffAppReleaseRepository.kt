package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.auth.staffCurrentIdToken
import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FirestoreService
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.StaffAppReleaseFirestoreDocument
import jp.co.yumemi.quiz.droidkaigi.core.data.storage.defaultStaffDmgDownloadPath
import jp.co.yumemi.quiz.droidkaigi.core.data.storage.deleteLocalFile
import jp.co.yumemi.quiz.droidkaigi.core.data.storage.downloadAuthenticatedStorageObject
import jp.co.yumemi.quiz.droidkaigi.core.data.storage.openLocalFile
import jp.co.yumemi.quiz.droidkaigi.core.data.storage.sha256HexOfFile
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppRelease
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAppReleaseRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.coroutines.cancellation.CancellationException

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

        val actualSha = try {
            sha256HexOfFile(downloaded)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            deleteLocalFile(downloaded)
            return Result.failure(e)
        }
        if (!actualSha.equals(release.sha256, ignoreCase = true)) {
            deleteLocalFile(downloaded)
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
        } catch (e: CancellationException) {
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
