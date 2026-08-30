package jp.co.yumemi.quiz.droidkaigi.feature.quiz.home

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder

data class HomeUiState(
    val nickname: String = "",
    val isLoading: Boolean = false,
    /**
     * null = 未取得（初回ローディング）または取得失敗表示中。
     * false = スタッフにより非公開。再チェック中は直前の true/false を保持する。
     */
    val sitePublished: Boolean? = null,
    /** 受付状況の取得に失敗した（ネットワーク障害など）。受付前（false）とは区別する。 */
    val siteStatusCheckFailed: Boolean = false,
    /** null = 未取得または取得失敗。empty = 公開フォルダ 0 件。 */
    val publishedFolders: List<QuizFolder>? = null,
    val selectedFolderId: String? = null,
    val error: HomeError? = null,
) {
    val isSiteOpen: Boolean get() = sitePublished == true
}

sealed interface HomeError {
    data object EmptyNickname : HomeError
    data object NoPublishedFolders : HomeError
    data object NoFolderSelected : HomeError
    data class LoadFailed(val detail: String?) : HomeError
}

sealed interface HomeIntent {
    data class NicknameChanged(val value: String) : HomeIntent
    data class SelectPublishedFolder(val folderId: String) : HomeIntent
    data object StartQuiz : HomeIntent

    /** Home が再表示されたときに loading を解除（中断復帰後の二重開始防止フラグ残り対策）。 */
    data object Shown : HomeIntent

    /** 受付状況の取得失敗後の再試行。 */
    data object RetrySiteStatus : HomeIntent
}

sealed interface HomeEvent {
    data object NavigateToQuiz : HomeEvent
}
