package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class SetActiveQuizFolderUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(folderId: String) {
        quizCatalogRepository.setActiveFolderId(folderId)
    }
}
