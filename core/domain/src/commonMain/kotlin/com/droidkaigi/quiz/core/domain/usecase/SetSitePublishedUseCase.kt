package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class SetSitePublishedUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(published: Boolean) {
        quizCatalogRepository.setSitePublished(published)
    }
}
