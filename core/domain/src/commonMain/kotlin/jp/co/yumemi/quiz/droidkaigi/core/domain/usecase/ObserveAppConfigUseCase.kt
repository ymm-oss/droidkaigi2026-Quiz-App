package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.AppConfigStatus
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.QuizCatalogRepository
import kotlinx.coroutines.flow.Flow

class ObserveAppConfigUseCase(private val quizCatalogRepository: QuizCatalogRepository) {
    operator fun invoke(): Flow<AppConfigStatus> = quizCatalogRepository.observeAppConfig()
}
