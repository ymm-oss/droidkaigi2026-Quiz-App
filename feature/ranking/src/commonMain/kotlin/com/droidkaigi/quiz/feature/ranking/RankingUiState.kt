package com.droidkaigi.quiz.feature.ranking

import com.droidkaigi.quiz.core.domain.model.RankingEntry

data class RankingUiState(
    val entries: List<RankingEntry> = emptyList(),
    val highlightNickname: String? = null,
    val isLoading: Boolean = true,
)

sealed interface RankingIntent {
    data object Refresh : RankingIntent
    data object GoHome : RankingIntent
}

sealed interface RankingEvent {
    data object NavigateHome : RankingEvent
}
