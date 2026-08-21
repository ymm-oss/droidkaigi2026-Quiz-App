package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppUpdateStatus
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.LocalStaffAppVersionProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAppReleaseRepository

class CheckForStaffAppUpdateUseCase(
    private val staffAppReleaseRepository: StaffAppReleaseRepository,
    private val localStaffAppVersionProvider: LocalStaffAppVersionProvider,
) {
    suspend operator fun invoke(): StaffAppUpdateStatus {
        val latest = staffAppReleaseRepository.fetchLatestRelease()
            ?: return StaffAppUpdateStatus.Unavailable
        val local = localStaffAppVersionProvider.current()
        return if (latest.versionCode > local.versionCode) {
            StaffAppUpdateStatus.UpdateAvailable(latest)
        } else {
            StaffAppUpdateStatus.UpToDate
        }
    }
}
