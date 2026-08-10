package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.model.LocalStaffAppVersion
import com.droidkaigi.quiz.core.domain.repository.LocalStaffAppVersionProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/** Staff Desktop auto-update is JVM-only; Wasm builds still need a Metro binding. */
@Inject
@ContributesBinding(AppScope::class)
class UnsupportedLocalStaffAppVersionProvider : LocalStaffAppVersionProvider {
    override fun current(): LocalStaffAppVersion = LocalStaffAppVersion(version = "0.0.0", versionCode = 0)
}
