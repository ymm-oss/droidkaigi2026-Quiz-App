package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class ListQuizFoldersUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(): List<QuizFolder> = quizCatalogRepository.listFolders()
}
