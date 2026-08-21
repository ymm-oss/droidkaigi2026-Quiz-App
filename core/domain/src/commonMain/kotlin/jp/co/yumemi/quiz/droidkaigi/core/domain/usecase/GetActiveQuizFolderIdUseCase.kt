package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class GetActiveQuizFolderIdUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(): String = quizCatalogRepository.getActiveFolderId()
}
