package com.droidkaigi.quiz.core.data.di

import com.droidkaigi.quiz.core.data.QuizSessionHolder
import com.droidkaigi.quiz.core.domain.repository.QuizRepository
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.usecase.GetDefaultQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase

/** Canonical Metro graph API (runtime-specific graph extends this in :composeApp). */
interface QuizAppGraph {
    val instantProvider: InstantProvider
    val quizRepository: QuizRepository
    val rankingRepository: RankingRepository
    val quizEngine: QuizEngine
    val sessionHolder: QuizSessionHolder
    val submitScoreUseCase: SubmitScoreUseCase
    val getTodayRankingsUseCase: GetTodayRankingsUseCase
    val getDefaultQuizSetUseCase: GetDefaultQuizSetUseCase
}
