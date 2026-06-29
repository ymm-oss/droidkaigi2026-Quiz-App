package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class CreateQuizFolderUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(name: String, description: String = ""): QuizFolder =
        quizCatalogRepository.createFolder(name, description)
}
