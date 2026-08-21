package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository

class SaveQuizSetUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    suspend operator fun invoke(quizSet: QuizSet) {
        quizCatalogRepository.saveQuizSet(quizSet)
    }
}
