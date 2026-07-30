package com.droidkaigi.quiz.feature.quiz.home

data class HomeUiState(
    val nickname: String = "",
    val isLoading: Boolean = false,
    val error: HomeError? = null,
)

sealed interface HomeError {
    data object EmptyNickname : HomeError
    data class LoadFailed(val detail: String?) : HomeError
}

sealed interface HomeIntent {
    data class NicknameChanged(val value: String) : HomeIntent
    data object StartQuiz : HomeIntent
    /** Home が再表示されたときに loading を解除（中断復帰後の二重開始防止フラグ残り対策）。 */
    data object Shown : HomeIntent
}

sealed interface HomeEvent {
    data object NavigateToQuiz : HomeEvent
}
