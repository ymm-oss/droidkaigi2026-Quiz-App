package jp.co.yumemi.quiz.droidkaigi.core.data.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn
import jp.co.yumemi.quiz.droidkaigi.core.data.InMemoryQuizCatalog

@ContributesTo(AppScope::class)
@BindingContainer
object FakeDataBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideInMemoryQuizCatalog(): InMemoryQuizCatalog = InMemoryQuizCatalog()
}
