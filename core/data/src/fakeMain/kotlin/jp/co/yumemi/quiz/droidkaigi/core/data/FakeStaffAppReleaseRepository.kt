package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppRelease
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAppReleaseRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class FakeStaffAppReleaseRepository : StaffAppReleaseRepository {
    override suspend fun fetchLatestRelease(): StaffAppRelease? = null

    override suspend fun downloadDmg(
        release: StaffAppRelease,
        onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit,
    ): Result<String> = Result.failure(UnsupportedOperationException("fake: no staff DMG download"))

    override fun openDownloadedFile(path: String) = Unit
}
