package com.droidkaigi.quiz.core.data.di

import com.droidkaigi.quiz.core.data.InMemoryQuizCatalog
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object FakeDataBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideInMemoryQuizCatalog(): InMemoryQuizCatalog = InMemoryQuizCatalog()
}
