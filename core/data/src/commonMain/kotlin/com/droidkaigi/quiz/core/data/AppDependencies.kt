package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.domain.repository.QuizRepository
import com.droidkaigi.quiz.core.domain.repository.RankingRepository
import com.droidkaigi.quiz.core.domain.session.QuizEngine
import com.droidkaigi.quiz.core.domain.time.InstantProvider
import com.droidkaigi.quiz.core.domain.time.SystemInstantProvider
import com.droidkaigi.quiz.core.domain.usecase.SubmitScoreUseCase

class AppDependencies(
    val instantProvider: InstantProvider = SystemInstantProvider(),
    val quizRepository: QuizRepository = JsonQuizRepository(),
    val rankingRepository: RankingRepository = FakeRankingRepository(instantProvider),
    val quizEngine: QuizEngine = QuizEngine(),
    val sessionHolder: QuizSessionHolder = QuizSessionHolder(),
) {
    val submitScoreUseCase: SubmitScoreUseCase =
        SubmitScoreUseCase(rankingRepository, instantProvider)

    companion object {
        val shared: AppDependencies = AppDependencies()
    }
}
