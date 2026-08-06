package com.droidkaigi.quiz.core.data.di

import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.usecase.GetSitePublishedUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetSitePublishedUseCase
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
@BindingContainer
object DataSitePublishBindings {
    @Provides
    fun provideGetSitePublishedUseCase(quizCatalogRepository: QuizCatalogRepository): GetSitePublishedUseCase =
        GetSitePublishedUseCase(quizCatalogRepository)

    @Provides
    fun provideSetSitePublishedUseCase(quizCatalogRepository: QuizCatalogRepository): SetSitePublishedUseCase =
        SetSitePublishedUseCase(quizCatalogRepository)
}
