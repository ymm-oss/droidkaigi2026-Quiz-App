package com.droidkaigi.quiz.feature.staff.ranking

import com.droidkaigi.quiz.core.domain.model.RankingEntry

data class StaffRankingUiState(
    val entries: List<RankingEntry> = emptyList(),
    val isLoading: Boolean = true,
    val isMutating: Boolean = false,
    val loadError: String? = null,
    val mutationError: String? = null,
)

sealed interface StaffRankingIntent {
    data object Refresh : StaffRankingIntent
    data class DeleteEntry(val entryId: String) : StaffRankingIntent
    data object ClearToday : StaffRankingIntent
}
