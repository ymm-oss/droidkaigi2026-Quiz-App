package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class UpdateQuizFolderUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(folder: QuizFolder) {
        quizCatalogRepository.updateFolder(folder)
    }
}
