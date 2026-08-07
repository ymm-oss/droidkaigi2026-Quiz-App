package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.StaffAppUpdateStatus
import com.droidkaigi.quiz.core.domain.repository.LocalStaffAppVersionProvider
import com.droidkaigi.quiz.core.domain.repository.StaffAppReleaseRepository

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
