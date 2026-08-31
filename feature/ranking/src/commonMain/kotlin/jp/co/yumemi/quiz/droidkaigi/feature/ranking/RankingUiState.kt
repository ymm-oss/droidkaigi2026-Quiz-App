package jp.co.yumemi.quiz.droidkaigi.feature.ranking

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry

data class RankingUiState(
    val entries: List<RankingEntry> = emptyList(),
    val highlightNickname: String? = null,
    val publishedFolders: List<QuizFolder> = emptyList(),
    val selectedFolderId: String? = null,
    val isLoading: Boolean = true,
    val error: RankingError? = null,
)

sealed interface RankingError {
    data class LoadFailed(val detail: String?) : RankingError
}

sealed interface RankingIntent {
    data object Refresh : RankingIntent
    data class SelectFolder(val folderId: String) : RankingIntent
    data object GoHome : RankingIntent
}

sealed interface RankingEvent {
    data object NavigateHome : RankingEvent
}
