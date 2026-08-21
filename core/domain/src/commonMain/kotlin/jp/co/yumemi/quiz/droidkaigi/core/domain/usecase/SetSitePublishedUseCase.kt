package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class SetSitePublishedUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(published: Boolean) {
        quizCatalogRepository.setSitePublished(published)
    }
}
