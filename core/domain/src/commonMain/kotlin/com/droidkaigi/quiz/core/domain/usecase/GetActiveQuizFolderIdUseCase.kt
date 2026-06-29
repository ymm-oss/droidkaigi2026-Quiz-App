package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class GetActiveQuizFolderIdUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(): String = quizCatalogRepository.getActiveFolderId()
}
