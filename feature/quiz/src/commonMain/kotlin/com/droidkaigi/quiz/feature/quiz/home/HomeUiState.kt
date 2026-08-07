package com.droidkaigi.quiz.feature.quiz.home

data class HomeUiState(
    val nickname: String = "",
    val isLoading: Boolean = false,
    /** null while checking or when the check failed; false = closed by staff. */
    val sitePublished: Boolean? = null,
    /** 受付状況の取得に失敗した（ネットワーク障害など）。受付前（false）とは区別する。 */
    val siteStatusCheckFailed: Boolean = false,
    val error: HomeError? = null,
) {
    val isSiteOpen: Boolean get() = sitePublished == true
}

sealed interface HomeError {
    data object EmptyNickname : HomeError
    data class LoadFailed(val detail: String?) : HomeError
}

sealed interface HomeIntent {
    data class NicknameChanged(val value: String) : HomeIntent
    data object StartQuiz : HomeIntent

    /** Home が再表示されたときに loading を解除（中断復帰後の二重開始防止フラグ残り対策）。 */
    data object Shown : HomeIntent

    /** 受付状況の取得失敗後の再試行。 */
    data object RetrySiteStatus : HomeIntent
}

sealed interface HomeEvent {
    data object NavigateToQuiz : HomeEvent
}
