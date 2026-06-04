package com.droidkaigi.quiz.core.data.di

import com.droidkaigi.quiz.core.data.QuizSessionHolder
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.time.SystemInstantProvider
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppScope::class)
@BindingContainer
object DataCommonBindings {
    @Provides
    @SingleIn(AppScope::class)
    fun provideSessionHolder(): QuizSessionHolder = QuizSessionHolder()

    @Provides
    fun provideQuizEngine(): QuizEngine = QuizEngine()

    @Provides
    fun provideInstantProvider(): InstantProvider = SystemInstantProvider()

    @Provides
    fun provideSubmitScoreUseCase(
        rankingRepository: RankingRepository,
        instantProvider: InstantProvider,
    ): SubmitScoreUseCase = SubmitScoreUseCase(rankingRepository, instantProvider)
}
