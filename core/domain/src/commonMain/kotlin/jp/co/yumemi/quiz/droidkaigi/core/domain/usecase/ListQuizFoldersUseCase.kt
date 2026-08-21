package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class ListQuizFoldersUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(): List<QuizFolder> = quizCatalogRepository.listFolders()
}
