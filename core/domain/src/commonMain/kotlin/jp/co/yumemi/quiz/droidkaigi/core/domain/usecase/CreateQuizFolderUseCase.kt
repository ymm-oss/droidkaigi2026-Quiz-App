package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class CreateQuizFolderUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(name: String, description: String = ""): QuizFolder =
        quizCatalogRepository.createFolder(name, description)
}
