package com.droidkaigi.quiz.feature.staff.quiz

import com.droidkaigi.quiz.core.domain.model.QuizSet

data class StaffQuizUiState(
    val quizSet: QuizSet? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface StaffQuizIntent {
    data object Refresh : StaffQuizIntent
}
