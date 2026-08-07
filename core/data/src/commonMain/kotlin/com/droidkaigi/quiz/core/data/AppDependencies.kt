package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.QuizAppGraph
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.usecase.CheckForStaffAppUpdateUseCase
import com.droidkaigi.quiz.core.domain.usecase.ClearTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.CreateQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteRankingEntryUseCase
import com.droidkaigi.quiz.core.domain.usecase.DownloadStaffAppUpdateUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetActiveQuizFolderIdUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetQuizSetForFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetSitePublishedUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetStaffAuthStateUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.ListQuizFoldersUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuickSignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuizPlayUseCase
import com.droidkaigi.quiz.core.domain.usecase.SaveQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetActiveQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetSitePublishedUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignOutStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.UpdateQuizFolderUseCase

/**
 * Facade over the Metro [QuizAppGraph]. Initialize once via [init] from the app entry point.
 */
class AppDependencies(
    val instantProvider: InstantProvider,
    private val quizCatalogRepository: QuizCatalogRepository,
    val quizEngine: QuizEngine,
    val sessionHolder: QuizSessionHolder,
    val quizPlayUseCase: QuizPlayUseCase,
    val getTodayRankingsUseCase: GetTodayRankingsUseCase,
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
    val getStaffAuthStateUseCase: GetStaffAuthStateUseCase,
    val signOutStaffUseCase: SignOutStaffUseCase,
    val checkForStaffAppUpdateUseCase: CheckForStaffAppUpdateUseCase,
    val downloadStaffAppUpdateUseCase: DownloadStaffAppUpdateUseCase,
) {
    val getSitePublishedUseCase: GetSitePublishedUseCase
        get() = GetSitePublishedUseCase(quizCatalogRepository)

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
                quizPlayUseCase = graph.quizPlayUseCase,
                getTodayRankingsUseCase = graph.getTodayRankingsUseCase,
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
