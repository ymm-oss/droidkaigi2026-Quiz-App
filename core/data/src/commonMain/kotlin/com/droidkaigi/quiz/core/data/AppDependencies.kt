package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.QuizAppGraph
import com.droidkaigi.quiz.core.domain.repository.QuizRepository
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.usecase.GetDefaultQuizSetUseCase
import com.droidkaigi.quiz.core.domain.usecase.GetTodayRankingsUseCase
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase

/**
 * Facade over the Metro [QuizAppGraph]. Initialize once via [init] from the app entry point.
 */
class AppDependencies(
    val instantProvider: InstantProvider,
    val quizRepository: QuizRepository,
    val rankingRepository: RankingRepository,
    val quizEngine: QuizEngine,
    val sessionHolder: QuizSessionHolder,
    val submitScoreUseCase: SubmitScoreUseCase,
    val getTodayRankingsUseCase: GetTodayRankingsUseCase,
    val getDefaultQuizSetUseCase: GetDefaultQuizSetUseCase,
) {
    companion object {
        lateinit var shared: AppDependencies
            private set

        fun init(graph: QuizAppGraph) {
            shared = AppDependencies(
                instantProvider = graph.instantProvider,
                quizRepository = graph.quizRepository,
                rankingRepository = graph.rankingRepository,
                quizEngine = graph.quizEngine,
                sessionHolder = graph.sessionHolder,
                submitScoreUseCase = graph.submitScoreUseCase,
                getTodayRankingsUseCase = graph.getTodayRankingsUseCase,
                getDefaultQuizSetUseCase = graph.getDefaultQuizSetUseCase,
            )
        }

        val isInitialized: Boolean
            get() = this::shared.isInitialized
    }
}
