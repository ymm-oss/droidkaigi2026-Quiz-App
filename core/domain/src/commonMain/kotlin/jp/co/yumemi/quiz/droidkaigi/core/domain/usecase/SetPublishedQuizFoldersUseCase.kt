package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class SetPublishedQuizFoldersUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(folderIds: List<String>) {
        quizCatalogRepository.setPublishedFolderIds(folderIds)
    }
}
