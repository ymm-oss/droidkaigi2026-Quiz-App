package jp.co.yumemi.quiz.droidkaigi.feature.staff

import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.QuizSessionHolder
import jp.co.yumemi.quiz.droidkaigi.core.data.SiteStatusHolder
import jp.co.yumemi.quiz.droidkaigi.core.data.StaffAuthHolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffSession
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.LocalStaffAppVersionProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAppReleaseRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAuthRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.session.QuizEngine
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.CheckForStaffAppUpdateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ClearTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.CreateQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.DeleteQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.DeleteRankingEntryUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.DownloadStaffAppUpdateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetActiveQuizFolderIdUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetQuizSetForFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetStaffAuthStateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ListQuizFoldersUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuickSignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuizPlayUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.RestoreStaffAuthSessionUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SaveQuizSetUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SetActiveQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignOutStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SubmitScoreUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.UpdateQuizFolderUseCase

/** Wires staff ViewModels against test doubles without pulling in a Metro graph. */
internal fun staffTestAppDependencies(
    catalogRepository: QuizCatalogRepository,
    rankingRepository: RankingRepository,
    instantProvider: InstantProvider = object : InstantProvider {
        override fun nowEpochMillis(): Long = 0L
    },
): AppDependencies {
    val staffAuthRepository = object : StaffAuthRepository {
        override suspend fun signIn(email: String, password: String): Result<StaffSession> =
            Result.failure(IllegalStateException("unused"))
    }
    val staffAuthHolder = StaffAuthHolder()
    val signInStaffUseCase = SignInStaffUseCase(staffAuthRepository, staffAuthHolder)
    val quizEngine = QuizEngine()
    val sessionHolder = QuizSessionHolder()
    val unusedReleaseRepository = object : StaffAppReleaseRepository {
        override suspend fun fetchLatestRelease() = null
        override suspend fun downloadDmg(
            release: jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppRelease,
            onProgress: (Long, Long?) -> Unit,
        ) = Result.failure<String>(UnsupportedOperationException())
        override fun openDownloadedFile(path: String) = Unit
    }
    return AppDependencies(
        instantProvider = instantProvider,
        quizCatalogRepository = catalogRepository,
        quizEngine = quizEngine,
        sessionHolder = sessionHolder,
        siteStatusHolder = SiteStatusHolder(),
        quizPlayUseCase = QuizPlayUseCase(
            quizEngine = quizEngine,
            sessionStore = sessionHolder,
            submitScoreUseCase = SubmitScoreUseCase(rankingRepository),
            instantProvider = instantProvider,
        ),
        getTodayRankingsUseCase = GetTodayRankingsUseCase(rankingRepository),
        deleteRankingEntryUseCase = DeleteRankingEntryUseCase(rankingRepository),
        clearTodayRankingsUseCase = ClearTodayRankingsUseCase(rankingRepository),
        listQuizFoldersUseCase = ListQuizFoldersUseCase(catalogRepository),
        createQuizFolderUseCase = CreateQuizFolderUseCase(catalogRepository),
        updateQuizFolderUseCase = UpdateQuizFolderUseCase(catalogRepository),
        deleteQuizFolderUseCase = DeleteQuizFolderUseCase(catalogRepository),
        getQuizSetForFolderUseCase = GetQuizSetForFolderUseCase(catalogRepository),
        saveQuizSetUseCase = SaveQuizSetUseCase(catalogRepository),
        getActiveQuizFolderIdUseCase = GetActiveQuizFolderIdUseCase(catalogRepository),
        setActiveQuizFolderUseCase = SetActiveQuizFolderUseCase(catalogRepository),
        signInStaffUseCase = signInStaffUseCase,
        quickSignInStaffUseCase = QuickSignInStaffUseCase(staffAuthRepository, signInStaffUseCase),
        restoreStaffAuthSessionUseCase = RestoreStaffAuthSessionUseCase(staffAuthRepository, staffAuthHolder),
        getStaffAuthStateUseCase = GetStaffAuthStateUseCase(staffAuthHolder),
        signOutStaffUseCase = SignOutStaffUseCase(staffAuthHolder, staffAuthRepository),
        checkForStaffAppUpdateUseCase = CheckForStaffAppUpdateUseCase(
            staffAppReleaseRepository = unusedReleaseRepository,
            localStaffAppVersionProvider = object : LocalStaffAppVersionProvider {
                override fun current() =
                    jp.co.yumemi.quiz.droidkaigi.core.domain.model.LocalStaffAppVersion("1.0.0", 10_000)
            },
        ),
        downloadStaffAppUpdateUseCase = DownloadStaffAppUpdateUseCase(
            staffAppReleaseRepository = unusedReleaseRepository,
        ),
    )
}
