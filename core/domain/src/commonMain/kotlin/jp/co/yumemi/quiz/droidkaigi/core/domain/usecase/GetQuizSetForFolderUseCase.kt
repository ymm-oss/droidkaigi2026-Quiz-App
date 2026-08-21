package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class GetQuizSetForFolderUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(folderId: String): QuizSet = quizCatalogRepository.getQuizSet(folderId)
}
