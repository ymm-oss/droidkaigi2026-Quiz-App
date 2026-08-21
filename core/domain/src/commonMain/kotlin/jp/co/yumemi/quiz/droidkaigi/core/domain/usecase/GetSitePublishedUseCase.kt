package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class GetSitePublishedUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(): Boolean = quizCatalogRepository.getSitePublished()
}
