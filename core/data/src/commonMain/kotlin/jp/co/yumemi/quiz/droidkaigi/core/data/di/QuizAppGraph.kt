package jp.co.yumemi.quiz.droidkaigi.core.data.di

import jp.co.yumemi.quiz.droidkaigi.core.data.QuizSessionHolder
import jp.co.yumemi.quiz.droidkaigi.core.data.SiteStatusHolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.RankingRepository
import jp.co.yumemi.quiz.droidkaigi.core.domain.session.QuizEngine
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.InstantProvider
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ClearTodayRankingsUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.CheckForStaffAppUpdateUseCase
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
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.RestoreStaffAuthSessionUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.QuizPlayUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SaveQuizSetUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SetActiveQuizFolderUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignInStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SignOutStaffUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SubmitScoreUseCase
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.UpdateQuizFolderUseCase

/** Canonical Metro graph API (runtime-specific graph extends this in :composeApp). */
interface QuizAppGraph {
    val instantProvider: InstantProvider
    val quizCatalogRepository: QuizCatalogRepository
    val rankingRepository: RankingRepository
    val quizEngine: QuizEngine
    val sessionHolder: QuizSessionHolder
    val siteStatusHolder: SiteStatusHolder
    val submitScoreUseCase: SubmitScoreUseCase
    val quizPlayUseCase: QuizPlayUseCase
    val getTodayRankingsUseCase: GetTodayRankingsUseCase
    val deleteRankingEntryUseCase: DeleteRankingEntryUseCase
    val clearTodayRankingsUseCase: ClearTodayRankingsUseCase
    val listQuizFoldersUseCase: ListQuizFoldersUseCase
    val createQuizFolderUseCase: CreateQuizFolderUseCase
    val updateQuizFolderUseCase: UpdateQuizFolderUseCase
    val deleteQuizFolderUseCase: DeleteQuizFolderUseCase
    val getQuizSetForFolderUseCase: GetQuizSetForFolderUseCase
    val saveQuizSetUseCase: SaveQuizSetUseCase
    val getActiveQuizFolderIdUseCase: GetActiveQuizFolderIdUseCase
    val setActiveQuizFolderUseCase: SetActiveQuizFolderUseCase
    val signInStaffUseCase: SignInStaffUseCase
    val quickSignInStaffUseCase: QuickSignInStaffUseCase
    val restoreStaffAuthSessionUseCase: RestoreStaffAuthSessionUseCase
    val getStaffAuthStateUseCase: GetStaffAuthStateUseCase
    val signOutStaffUseCase: SignOutStaffUseCase
    val checkForStaffAppUpdateUseCase: CheckForStaffAppUpdateUseCase
    val downloadStaffAppUpdateUseCase: DownloadStaffAppUpdateUseCase
}
