package com.droidkaigi.quiz.core.data.di

import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.usecase.ClearTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteRankingEntryUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides

@ContributesTo(AppScope::class)
@BindingContainer
object RankingUseCaseBindings {
    @Provides
    fun provideSubmitScoreUseCase(
        rankingRepository: RankingRepository,
    ): SubmitScoreUseCase = SubmitScoreUseCase(rankingRepository)

    @Provides
    fun provideGetTodayRankingsUseCase(rankingRepository: RankingRepository): GetTodayRankingsUseCase =
        GetTodayRankingsUseCase(rankingRepository)

    @Provides
    fun provideDeleteRankingEntryUseCase(rankingRepository: RankingRepository): DeleteRankingEntryUseCase =
        DeleteRankingEntryUseCase(rankingRepository)

    @Provides
    fun provideClearTodayRankingsUseCase(rankingRepository: RankingRepository): ClearTodayRankingsUseCase =
        ClearTodayRankingsUseCase(rankingRepository)
}
