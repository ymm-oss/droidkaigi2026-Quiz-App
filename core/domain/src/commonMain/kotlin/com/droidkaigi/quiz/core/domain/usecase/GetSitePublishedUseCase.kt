package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.repository.QuizCatalogRepository

class GetSitePublishedUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(): Boolean = quizCatalogRepository.getSitePublished()
}
