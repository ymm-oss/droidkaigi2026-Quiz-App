package jp.co.yumemi.quiz.droidkaigi.core.data.di

import jp.co.yumemi.quiz.droidkaigi.core.data.QuizSessionHolder
import jp.co.yumemi.quiz.droidkaigi.core.data.SiteStatusHolder
import jp.co.yumemi.quiz.droidkaigi.core.data.StaffAuthHolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.LocalStaffAppVersionProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAuthRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAppReleaseRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.session.QuizEngine
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.SystemInstantProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.CheckForStaffAppUpdateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.CreateQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.DeleteQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.DownloadStaffAppUpdateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetActiveQuizFolderIdUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetQuizSetForFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetStaffAuthStateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ListQuizFoldersUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuickSignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.RestoreStaffAuthSessionUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuizPlayUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SaveQuizSetUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SetActiveQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignOutStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SubmitScoreUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.UpdateQuizFolderUseCase
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
    fun provideSiteStatusHolder(): SiteStatusHolder = SiteStatusHolder()

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
    fun provideRestoreStaffAuthSessionUseCase(
        staffAuthRepository: StaffAuthRepository,
        staffAuthHolder: StaffAuthHolder,
    ): RestoreStaffAuthSessionUseCase = RestoreStaffAuthSessionUseCase(staffAuthRepository, staffAuthHolder)

    @Provides
    fun provideSignOutStaffUseCase(
        staffAuthHolder: StaffAuthHolder,
        staffAuthRepository: StaffAuthRepository,
    ): SignOutStaffUseCase = SignOutStaffUseCase(staffAuthHolder, staffAuthRepository)

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
