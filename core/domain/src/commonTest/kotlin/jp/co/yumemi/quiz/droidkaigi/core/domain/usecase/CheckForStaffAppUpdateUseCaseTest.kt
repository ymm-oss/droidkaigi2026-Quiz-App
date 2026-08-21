package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.LocalStaffAppVersion
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppRelease
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppUpdateStatus
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.LocalStaffAppVersionProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAppReleaseRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlinx.coroutines.runBlocking

class CheckForStaffAppUpdateUseCaseTest {
    @Test
    fun returnsUnavailableWhenNoRemoteRelease() = runBlocking {
        val useCase = CheckForStaffAppUpdateUseCase(
            staffAppReleaseRepository = FakeReleaseRepo(null),
            localStaffAppVersionProvider = FixedVersion(LocalStaffAppVersion("1.0.0", 10_000)),
        )
        assertEquals(StaffAppUpdateStatus.Unavailable, useCase())
    }

    @Test
    fun returnsUpToDateWhenRemoteNotNewer() = runBlocking {
        val release = sampleRelease(versionCode = 10_000)
        val useCase = CheckForStaffAppUpdateUseCase(
            staffAppReleaseRepository = FakeReleaseRepo(release),
            localStaffAppVersionProvider = FixedVersion(LocalStaffAppVersion("1.0.0", 10_000)),
        )
        assertEquals(StaffAppUpdateStatus.UpToDate, useCase())
    }

    @Test
    fun returnsUpdateAvailableWhenRemoteNewer() = runBlocking {
        val release = sampleRelease(version = "1.1.0", versionCode = 10_100)
        val useCase = CheckForStaffAppUpdateUseCase(
            staffAppReleaseRepository = FakeReleaseRepo(release),
            localStaffAppVersionProvider = FixedVersion(LocalStaffAppVersion("1.0.0", 10_000)),
        )
        val status = useCase()
        assertIs<StaffAppUpdateStatus.UpdateAvailable>(status)
        assertEquals(release, status.release)
    }

    private fun sampleRelease(
        version: String = "1.0.0",
        versionCode: Int = 10_000,
    ) = StaffAppRelease(
        version = version,
        versionCode = versionCode,
        storagePath = "releases/staff-desktop/$version.dmg",
        sha256 = "abc",
    )

    private class FakeReleaseRepo(
        private val release: StaffAppRelease?,
    ) : StaffAppReleaseRepository {
        override suspend fun fetchLatestRelease(): StaffAppRelease? = release
        override suspend fun downloadDmg(
            release: StaffAppRelease,
            onProgress: (Long, Long?) -> Unit,
        ): Result<String> = Result.failure(UnsupportedOperationException())
        override fun openDownloadedFile(path: String) = Unit
    }

    private class FixedVersion(
        private val version: LocalStaffAppVersion,
    ) : LocalStaffAppVersionProvider {
        override fun current(): LocalStaffAppVersion = version
    }
}
