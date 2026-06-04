package com.droidkaigi.quiz.feature.staff.ranking

import com.droidkaigi.quiz.core.domain.model.RankingEntry

data class StaffRankingUiState(
    val entries: List<RankingEntry> = emptyList(),
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)

sealed interface StaffRankingIntent {
    data object Refresh : StaffRankingIntent
}
