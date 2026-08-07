package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.StaffAppRelease
import com.droidkaigi.quiz.core.domain.repository.StaffAppReleaseRepository

class DownloadStaffAppUpdateUseCase(
    private val staffAppReleaseRepository: StaffAppReleaseRepository,
) {
    suspend operator fun invoke(
        release: StaffAppRelease,
        onProgress: (bytesRead: Long, totalBytes: Long?) -> Unit = { _, _ -> },
        openAfterDownload: Boolean = true,
    ): Result<String> {
        val path = staffAppReleaseRepository.downloadDmg(release, onProgress).getOrElse {
            return Result.failure(it)
        }
        if (openAfterDownload) {
            runCatching { staffAppReleaseRepository.openDownloadedFile(path) }
        }
        return Result.success(path)
    }
}
