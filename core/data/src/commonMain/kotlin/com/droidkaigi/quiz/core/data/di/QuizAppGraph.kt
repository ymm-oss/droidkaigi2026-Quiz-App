package com.droidkaigi.quiz.core.data.di

import com.droidkaigi.quiz.core.data.QuizSessionHolder
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.usecase.CreateQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetActiveQuizFolderIdUseCase
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

/** Canonical Metro graph API (runtime-specific graph extends this in :composeApp). */
interface QuizAppGraph {
    val instantProvider: InstantProvider
    val quizCatalogRepository: QuizCatalogRepository
    val rankingRepository: RankingRepository
    val quizEngine: QuizEngine
    val sessionHolder: QuizSessionHolder
    val submitScoreUseCase: SubmitScoreUseCase
    val getTodayRankingsUseCase: GetTodayRankingsUseCase
    val listQuizFoldersUseCase: ListQuizFoldersUseCase
    val createQuizFolderUseCase: CreateQuizFolderUseCase
    val updateQuizFolderUseCase: UpdateQuizFolderUseCase
    val deleteQuizFolderUseCase: DeleteQuizFolderUseCase
    val getQuizSetForFolderUseCase: GetQuizSetForFolderUseCase
    val saveQuizSetUseCase: SaveQuizSetUseCase
    val getActiveQuizFolderIdUseCase: GetActiveQuizFolderIdUseCase
    val setActiveQuizFolderUseCase: SetActiveQuizFolderUseCase
    val signInStaffUseCase: SignInStaffUseCase
    val getStaffAuthStateUseCase: GetStaffAuthStateUseCase
    val signOutStaffUseCase: SignOutStaffUseCase
}
