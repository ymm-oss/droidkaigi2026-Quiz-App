package jp.co.yumemi.quiz.droidkaigi.core.data.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import jp.co.yumemi.quiz.droidkaigi.core.data.SiteStatusHolder

@ContributesTo(AppScope::class)
@BindingContainer
object SiteStatusBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideSiteStatusHolder(): SiteStatusHolder = SiteStatusHolder()
}
