package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class UpdateQuizFolderUseCase(
    private val quizCatalogRepository: QuizCatalogRepository,
) {
    suspend operator fun invoke(folder: QuizFolder) {
        quizCatalogRepository.updateFolder(folder)
    }
}
