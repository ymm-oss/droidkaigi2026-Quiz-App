package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.LocalStaffAppVersion
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.LocalStaffAppVersionProvider
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
