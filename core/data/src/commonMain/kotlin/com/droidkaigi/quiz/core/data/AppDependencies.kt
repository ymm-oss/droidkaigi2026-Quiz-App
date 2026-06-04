package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.QuizAppGraph
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.repository.QuizRepository
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.usecase.CreateQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetActiveQuizFolderIdUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetDefaultQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetQuizSetForFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.ListQuizFoldersUseCase
import com.droidkaigi.quiz.core.domain.usecase.SaveQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetStaffAuthStateUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetActiveQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignOutStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import com.droidkaigi.quiz.core.domain.usecase.UpdateQuizFolderUseCase

/**
 * Facade over the Metro [QuizAppGraph]. Initialize once via [init] from the app entry point.
 */
class AppDependencies(
    val instantProvider: InstantProvider,
    val quizRepository: QuizRepository,
    val quizCatalogRepository: QuizCatalogRepository,
    val rankingRepository: RankingRepository,
    val quizEngine: QuizEngine,
    val sessionHolder: QuizSessionHolder,
    val submitScoreUseCase: SubmitScoreUseCase,
    val getTodayRankingsUseCase: GetTodayRankingsUseCase,
    val getDefaultQuizSetUseCase: GetDefaultQuizSetUseCase,
    val listQuizFoldersUseCase: ListQuizFoldersUseCase,
    val createQuizFolderUseCase: CreateQuizFolderUseCase,
    val updateQuizFolderUseCase: UpdateQuizFolderUseCase,
    val deleteQuizFolderUseCase: DeleteQuizFolderUseCase,
    val getQuizSetForFolderUseCase: GetQuizSetForFolderUseCase,
    val saveQuizSetUseCase: SaveQuizSetUseCase,
    val getActiveQuizFolderIdUseCase: GetActiveQuizFolderIdUseCase,
    val setActiveQuizFolderUseCase: SetActiveQuizFolderUseCase,
    val signInStaffUseCase: SignInStaffUseCase,
    val getStaffAuthStateUseCase: GetStaffAuthStateUseCase,
    val signOutStaffUseCase: SignOutStaffUseCase,
) {
    companion object {
        lateinit var shared: AppDependencies
            private set

        fun init(graph: QuizAppGraph) {
            shared = AppDependencies(
                instantProvider = graph.instantProvider,
                quizRepository = graph.quizRepository,
                quizCatalogRepository = graph.quizCatalogRepository,
                rankingRepository = graph.rankingRepository,
                quizEngine = graph.quizEngine,
                sessionHolder = graph.sessionHolder,
                submitScoreUseCase = graph.submitScoreUseCase,
                getTodayRankingsUseCase = graph.getTodayRankingsUseCase,
                getDefaultQuizSetUseCase = graph.getDefaultQuizSetUseCase,
                listQuizFoldersUseCase = graph.listQuizFoldersUseCase,
                createQuizFolderUseCase = graph.createQuizFolderUseCase,
                updateQuizFolderUseCase = graph.updateQuizFolderUseCase,
                deleteQuizFolderUseCase = graph.deleteQuizFolderUseCase,
                getQuizSetForFolderUseCase = graph.getQuizSetForFolderUseCase,
                saveQuizSetUseCase = graph.saveQuizSetUseCase,
                getActiveQuizFolderIdUseCase = graph.getActiveQuizFolderIdUseCase,
                setActiveQuizFolderUseCase = graph.setActiveQuizFolderUseCase,
                signInStaffUseCase = graph.signInStaffUseCase,
                getStaffAuthStateUseCase = graph.getStaffAuthStateUseCase,
                signOutStaffUseCase = graph.signOutStaffUseCase,
            )
        }

        val isInitialized: Boolean
            get() = this::shared.isInitialized
    }
}
