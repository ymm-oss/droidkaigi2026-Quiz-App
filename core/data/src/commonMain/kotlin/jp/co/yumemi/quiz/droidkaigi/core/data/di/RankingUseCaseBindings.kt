package jp.co.yumemi.quiz.droidkaigi.core.data.di

import dev.zacsweers.metro.BindingContainer
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ClearTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.DeleteRankingEntryUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ObserveTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SubmitScoreUseCase

@ContributesTo(AppScope::class)
@BindingContainer
object RankingUseCaseBindings {
    @Provides
    fun provideSubmitScoreUseCase(rankingRepository: RankingRepository): SubmitScoreUseCase =
        SubmitScoreUseCase(rankingRepository)

    @Provides
    fun provideGetTodayRankingsUseCase(rankingRepository: RankingRepository): GetTodayRankingsUseCase =
        GetTodayRankingsUseCase(rankingRepository)

    @Provides
    fun provideObserveTodayRankingsUseCase(rankingRepository: RankingRepository): ObserveTodayRankingsUseCase =
        ObserveTodayRankingsUseCase(rankingRepository)

    @Provides
    fun provideDeleteRankingEntryUseCase(rankingRepository: RankingRepository): DeleteRankingEntryUseCase =
        DeleteRankingEntryUseCase(rankingRepository)

    @Provides
    fun provideClearTodayRankingsUseCase(rankingRepository: RankingRepository): ClearTodayRankingsUseCase =
        ClearTodayRankingsUseCase(rankingRepository)
}
