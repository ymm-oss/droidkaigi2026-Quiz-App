package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.di.QuizAppGraph
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
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
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetSitePublishedUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ObserveAppConfigUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetStaffAuthStateUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.GetTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ObserveTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ListQuizFoldersUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuickSignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuizPlayUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.RestoreStaffAuthSessionUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SaveQuizSetUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SetActiveQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SetSitePublishedUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignOutStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.UpdateQuizFolderUseCase

/**
 * Facade over the Metro [QuizAppGraph]. Initialize once via [init] from the app entry point.
 */
@Suppress("LongParameterList") // Metro graph facade; grows with provided use cases.
class AppDependencies(
    val instantProvider: InstantProvider,
    private val quizCatalogRepository: QuizCatalogRepository,
    val quizEngine: QuizEngine,
    val sessionHolder: QuizSessionHolder,
    val siteStatusHolder: SiteStatusHolder,
    val quizPlayUseCase: QuizPlayUseCase,
    val getTodayRankingsUseCase: GetTodayRankingsUseCase,
    val observeTodayRankingsUseCase: ObserveTodayRankingsUseCase,
    val deleteRankingEntryUseCase: DeleteRankingEntryUseCase,
    val clearTodayRankingsUseCase: ClearTodayRankingsUseCase,
    val listQuizFoldersUseCase: ListQuizFoldersUseCase,
    val createQuizFolderUseCase: CreateQuizFolderUseCase,
    val updateQuizFolderUseCase: UpdateQuizFolderUseCase,
    val deleteQuizFolderUseCase: DeleteQuizFolderUseCase,
    val getQuizSetForFolderUseCase: GetQuizSetForFolderUseCase,
    val saveQuizSetUseCase: SaveQuizSetUseCase,
    val getActiveQuizFolderIdUseCase: GetActiveQuizFolderIdUseCase,
    val setActiveQuizFolderUseCase: SetActiveQuizFolderUseCase,
    val signInStaffUseCase: SignInStaffUseCase,
    val quickSignInStaffUseCase: QuickSignInStaffUseCase,
    val restoreStaffAuthSessionUseCase: RestoreStaffAuthSessionUseCase,
    val getStaffAuthStateUseCase: GetStaffAuthStateUseCase,
    val signOutStaffUseCase: SignOutStaffUseCase,
    val checkForStaffAppUpdateUseCase: CheckForStaffAppUpdateUseCase,
    val downloadStaffAppUpdateUseCase: DownloadStaffAppUpdateUseCase,
) {
    val getSitePublishedUseCase: GetSitePublishedUseCase
        get() = GetSitePublishedUseCase(quizCatalogRepository)

    val observeAppConfigUseCase: ObserveAppConfigUseCase
        get() = ObserveAppConfigUseCase(quizCatalogRepository)

    val setSitePublishedUseCase: SetSitePublishedUseCase
        get() = SetSitePublishedUseCase(quizCatalogRepository)

    companion object {
        lateinit var shared: AppDependencies
            private set

        fun init(graph: QuizAppGraph) {
            shared = AppDependencies(
                instantProvider = graph.instantProvider,
                quizCatalogRepository = graph.quizCatalogRepository,
                quizEngine = graph.quizEngine,
                sessionHolder = graph.sessionHolder,
                siteStatusHolder = graph.siteStatusHolder,
                quizPlayUseCase = graph.quizPlayUseCase,
                getTodayRankingsUseCase = graph.getTodayRankingsUseCase,
                observeTodayRankingsUseCase = graph.observeTodayRankingsUseCase,
                deleteRankingEntryUseCase = graph.deleteRankingEntryUseCase,
                clearTodayRankingsUseCase = graph.clearTodayRankingsUseCase,
                listQuizFoldersUseCase = graph.listQuizFoldersUseCase,
                createQuizFolderUseCase = graph.createQuizFolderUseCase,
                updateQuizFolderUseCase = graph.updateQuizFolderUseCase,
                deleteQuizFolderUseCase = graph.deleteQuizFolderUseCase,
                getQuizSetForFolderUseCase = graph.getQuizSetForFolderUseCase,
                saveQuizSetUseCase = graph.saveQuizSetUseCase,
                getActiveQuizFolderIdUseCase = graph.getActiveQuizFolderIdUseCase,
                setActiveQuizFolderUseCase = graph.setActiveQuizFolderUseCase,
                signInStaffUseCase = graph.signInStaffUseCase,
                quickSignInStaffUseCase = graph.quickSignInStaffUseCase,
                restoreStaffAuthSessionUseCase = graph.restoreStaffAuthSessionUseCase,
                getStaffAuthStateUseCase = graph.getStaffAuthStateUseCase,
                signOutStaffUseCase = graph.signOutStaffUseCase,
                checkForStaffAppUpdateUseCase = graph.checkForStaffAppUpdateUseCase,
                downloadStaffAppUpdateUseCase = graph.downloadStaffAppUpdateUseCase,
            )
        }

        val isInitialized: Boolean
            get() = this::shared.isInitialized
    }
}
