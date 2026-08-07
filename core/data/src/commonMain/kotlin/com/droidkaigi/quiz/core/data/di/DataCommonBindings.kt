package com.droidkaigi.quiz.core.data.di

import com.droidkaigi.quiz.core.data.QuizSessionHolder
import com.droidkaigi.quiz.core.data.StaffAuthHolder
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.repository.LocalStaffAppVersionProvider
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
import com.droidkaigi.quiz.core.domain.repository.StaffAppReleaseRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.time.SystemInstantProvider
import com.droidkaigi.quiz.core.domain.usecase.CheckForStaffAppUpdateUseCase
import com.droidkaigi.quiz.core.domain.usecase.CreateQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DownloadStaffAppUpdateUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetActiveQuizFolderIdUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetQuizSetForFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetStaffAuthStateUseCase
import com.droidkaigi.quiz.core.domain.usecase.ListQuizFoldersUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuickSignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuizPlayUseCase
import com.droidkaigi.quiz.core.domain.usecase.SaveQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetActiveQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignOutStaffUseCase
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
    @SingleIn(AppScope::class)
    fun provideStaffAuthHolder(): StaffAuthHolder = StaffAuthHolder()

    @Provides
    fun provideQuizEngine(): QuizEngine = QuizEngine()

    @Provides
    fun provideInstantProvider(): InstantProvider = SystemInstantProvider()

    @Provides
    fun provideQuizPlayUseCase(
        quizEngine: QuizEngine,
        sessionHolder: QuizSessionHolder,
        submitScoreUseCase: SubmitScoreUseCase,
        instantProvider: InstantProvider,
    ): QuizPlayUseCase = QuizPlayUseCase(
        quizEngine = quizEngine,
        sessionStore = sessionHolder,
        submitScoreUseCase = submitScoreUseCase,
        instantProvider = instantProvider,
    )

    @Provides
    fun provideListQuizFoldersUseCase(quizCatalogRepository: QuizCatalogRepository): ListQuizFoldersUseCase =
        ListQuizFoldersUseCase(quizCatalogRepository)

    @Provides
    fun provideCreateQuizFolderUseCase(quizCatalogRepository: QuizCatalogRepository): CreateQuizFolderUseCase =
        CreateQuizFolderUseCase(quizCatalogRepository)

    @Provides
    fun provideUpdateQuizFolderUseCase(quizCatalogRepository: QuizCatalogRepository): UpdateQuizFolderUseCase =
        UpdateQuizFolderUseCase(quizCatalogRepository)

    @Provides
    fun provideDeleteQuizFolderUseCase(quizCatalogRepository: QuizCatalogRepository): DeleteQuizFolderUseCase =
        DeleteQuizFolderUseCase(quizCatalogRepository)

    @Provides
    fun provideGetQuizSetForFolderUseCase(quizCatalogRepository: QuizCatalogRepository): GetQuizSetForFolderUseCase =
        GetQuizSetForFolderUseCase(quizCatalogRepository)

    @Provides
    fun provideSaveQuizSetUseCase(quizCatalogRepository: QuizCatalogRepository): SaveQuizSetUseCase =
        SaveQuizSetUseCase(quizCatalogRepository)

    @Provides
    fun provideGetActiveQuizFolderIdUseCase(
        quizCatalogRepository: QuizCatalogRepository,
    ): GetActiveQuizFolderIdUseCase = GetActiveQuizFolderIdUseCase(quizCatalogRepository)

    @Provides
    fun provideSetActiveQuizFolderUseCase(quizCatalogRepository: QuizCatalogRepository): SetActiveQuizFolderUseCase =
        SetActiveQuizFolderUseCase(quizCatalogRepository)

    @Provides
    fun provideSignInStaffUseCase(
        staffAuthRepository: StaffAuthRepository,
        staffAuthHolder: StaffAuthHolder,
    ): SignInStaffUseCase = SignInStaffUseCase(staffAuthRepository, staffAuthHolder)

    @Provides
    fun provideQuickSignInStaffUseCase(
        staffAuthRepository: StaffAuthRepository,
        signInStaffUseCase: SignInStaffUseCase,
    ): QuickSignInStaffUseCase = QuickSignInStaffUseCase(staffAuthRepository, signInStaffUseCase)

    @Provides
    fun provideGetStaffAuthStateUseCase(staffAuthHolder: StaffAuthHolder): GetStaffAuthStateUseCase =
        GetStaffAuthStateUseCase(staffAuthHolder)

    @Provides
    fun provideSignOutStaffUseCase(staffAuthHolder: StaffAuthHolder): SignOutStaffUseCase =
        SignOutStaffUseCase(staffAuthHolder)

    @Provides
    fun provideCheckForStaffAppUpdateUseCase(
        staffAppReleaseRepository: StaffAppReleaseRepository,
        localStaffAppVersionProvider: LocalStaffAppVersionProvider,
    ): CheckForStaffAppUpdateUseCase = CheckForStaffAppUpdateUseCase(
        staffAppReleaseRepository = staffAppReleaseRepository,
        localStaffAppVersionProvider = localStaffAppVersionProvider,
    )

    @Provides
    fun provideDownloadStaffAppUpdateUseCase(
        staffAppReleaseRepository: StaffAppReleaseRepository,
    ): DownloadStaffAppUpdateUseCase = DownloadStaffAppUpdateUseCase(staffAppReleaseRepository)
}
