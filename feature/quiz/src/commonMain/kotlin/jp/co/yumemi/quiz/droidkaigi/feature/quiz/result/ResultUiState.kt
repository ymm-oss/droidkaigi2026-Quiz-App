package jp.co.yumemi.quiz.droidkaigi.feature.quiz.result

data class ResultUiState(
    val nickname: String = "",
    val correctCount: Int = 0,
    val totalCount: Int = 0,
    val score: Int = 0,
    /** false = 表示できる結果がない（プロセス死復元などで lastResult が消えた）。 */
    val hasResult: Boolean = false,
)

sealed interface ResultIntent {
    data object GoToRanking : ResultIntent
}

sealed interface ResultEvent {
    data object NavigateToRanking : ResultEvent
}
