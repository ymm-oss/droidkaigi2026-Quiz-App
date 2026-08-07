package com.droidkaigi.quiz.feature.staff

import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.data.QuizSessionHolder
import com.droidkaigi.quiz.core.data.StaffAuthHolder
import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.usecase.ClearTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.CreateQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.DeleteRankingEntryUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetActiveQuizFolderIdUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetQuizSetForFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetStaffAuthStateUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.ListQuizFoldersUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuickSignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.QuizPlayUseCase
import com.droidkaigi.quiz.core.domain.usecase.SaveQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.SetActiveQuizFolderUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignInStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SignOutStaffUseCase
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase
import com.droidkaigi.quiz.core.domain.usecase.UpdateQuizFolderUseCase

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
    return AppDependencies(
        instantProvider = instantProvider,
        quizCatalogRepository = catalogRepository,
        quizEngine = quizEngine,
        sessionHolder = sessionHolder,
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
        getStaffAuthStateUseCase = GetStaffAuthStateUseCase(staffAuthHolder),
        signOutStaffUseCase = SignOutStaffUseCase(staffAuthHolder),
    )
}
