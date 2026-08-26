package jp.co.yumemi.quiz.droidkaigi.feature.staff.ranking

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry

data class StaffRankingUiState(
    val entries: List<RankingEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val loadError: String? = null,
    val mutationError: String? = null,
    val reloadWarning: String? = null,
)

sealed interface StaffRankingIntent {
    data object Refresh : StaffRankingIntent
    data object ClearMutationFeedback : StaffRankingIntent
    data class DeleteEntry(val entryId: String) : StaffRankingIntent
    data object ClearToday : StaffRankingIntent
}
