package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class GetQuizSetForFolderUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(folderId: String): QuizSet = quizCatalogRepository.getQuizSet(folderId)
}
