package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class SaveQuizSetUseCase(
    private val quizCatalogRepository: QuizCatalogRepository,
) {
    suspend operator fun invoke(quizSet: QuizSet) {
        quizCatalogRepository.saveQuizSet(quizSet)
    }
}
