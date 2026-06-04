package com.droidkaigi.quiz.feature.quiz.result

data class ResultUiState(
    val nickname: String = "",
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val animatedScore: Int = 0,
    val targetScore: Int = 0,
)

sealed interface ResultIntent {
    data object GoToRanking : ResultIntent
}

sealed interface ResultEvent {
    data object NavigateToRanking : ResultEvent
}
