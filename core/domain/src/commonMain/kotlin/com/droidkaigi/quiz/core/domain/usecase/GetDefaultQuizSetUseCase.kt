package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class GetDefaultQuizSetUseCase(
    private val quizCatalogRepository: QuizCatalogRepository,
) {
    suspend operator fun invoke(): QuizSet {
        val folderId = quizCatalogRepository.getActiveFolderId()
        return quizCatalogRepository.getQuizSet(folderId)
    }
}
