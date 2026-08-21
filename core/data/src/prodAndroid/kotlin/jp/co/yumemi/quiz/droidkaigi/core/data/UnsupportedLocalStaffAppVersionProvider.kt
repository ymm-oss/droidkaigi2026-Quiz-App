package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.LocalStaffAppVersion
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.LocalStaffAppVersionProvider
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

/** Staff Desktop auto-update is JVM-only; Android builds still need a Metro binding. */
@Inject
@ContributesBinding(AppScope::class)
class UnsupportedLocalStaffAppVersionProvider : LocalStaffAppVersionProvider {
    override fun current(): LocalStaffAppVersion = LocalStaffAppVersion(version = "0.0.0", versionCode = 0)
}
