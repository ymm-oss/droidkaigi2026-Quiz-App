package com.droidkaigi.quiz.core.data.di

import com.droidkaigi.quiz.core.data.QuizSessionHolder
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.time.SystemInstantProvider
import com.droidkaigi.quiz.core.domain.usecase.CreateQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetActiveQuizFolderIdUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetDefaultQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetQuizSetForFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.ListQuizFoldersUseCase
import com.droidkaigi.quiz.core.domain.usecase.SaveQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetActiveQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import com.droidkaigi.quiz.core.domain.usecase.UpdateQuizFolderUseCase
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

    @Provides
    fun provideGetTodayRankingsUseCase(
        rankingRepository: RankingRepository,
    ): GetTodayRankingsUseCase = GetTodayRankingsUseCase(rankingRepository)

    @Provides
    fun provideGetDefaultQuizSetUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): GetDefaultQuizSetUseCase = GetDefaultQuizSetUseCase(quizCatalogRepository)

    @Provides
    fun provideListQuizFoldersUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): ListQuizFoldersUseCase = ListQuizFoldersUseCase(quizCatalogRepository)

    @Provides
    fun provideCreateQuizFolderUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): CreateQuizFolderUseCase = CreateQuizFolderUseCase(quizCatalogRepository)

    @Provides
    fun provideUpdateQuizFolderUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): UpdateQuizFolderUseCase = UpdateQuizFolderUseCase(quizCatalogRepository)

    @Provides
    fun provideDeleteQuizFolderUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): DeleteQuizFolderUseCase = DeleteQuizFolderUseCase(quizCatalogRepository)

    @Provides
    fun provideGetQuizSetForFolderUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): GetQuizSetForFolderUseCase = GetQuizSetForFolderUseCase(quizCatalogRepository)

    @Provides
    fun provideSaveQuizSetUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): SaveQuizSetUseCase = SaveQuizSetUseCase(quizCatalogRepository)

    @Provides
    fun provideGetActiveQuizFolderIdUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): GetActiveQuizFolderIdUseCase = GetActiveQuizFolderIdUseCase(quizCatalogRepository)

    @Provides
    fun provideSetActiveQuizFolderUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): SetActiveQuizFolderUseCase = SetActiveQuizFolderUseCase(quizCatalogRepository)
}
