package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizRepository

class GetDefaultQuizSetUseCase(
    private val quizRepository: QuizRepository,
) {
    suspend operator fun invoke(): QuizSet = quizRepository.getDefaultQuizSet()
}
