package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class DeleteQuizFolderUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(folderId: String) {
        quizCatalogRepository.deleteFolder(folderId)
    }
}
