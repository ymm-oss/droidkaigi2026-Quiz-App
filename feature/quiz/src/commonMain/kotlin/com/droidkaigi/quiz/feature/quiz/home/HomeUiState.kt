package com.droidkaigi.quiz.feature.quiz.home

data class HomeUiState(val nickname: String = "", val isLoading: Boolean = false, val errorMessage: String? = null)

sealed interface HomeIntent {
    data class NicknameChanged(val value: String) : HomeIntent
    data object StartQuiz : HomeIntent
}

sealed interface HomeEvent {
    data object NavigateToQuiz : HomeEvent
}
