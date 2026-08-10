package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.LocalStaffAppVersion
import com.droidkaigi.quiz.core.domain.repository.LocalStaffAppVersionProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class FakeLocalStaffAppVersionProvider : LocalStaffAppVersionProvider {
    override fun current(): LocalStaffAppVersion = LocalStaffAppVersion(
        version = "1.0.0",
        versionCode = 10_000,
    )
}
