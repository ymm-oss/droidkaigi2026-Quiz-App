package com.droidkaigi.quiz.feature.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidkaigi.quiz.core.data.AppDependencies
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RankingViewModel(
    private val deps: AppDependencies = AppDependencies.shared,
) : ViewModel() {
    private val _uiState = MutableStateFlow(RankingUiState(highlightNickname = deps.sessionHolder.highlightNickname))
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RankingEvent>()
    val events: SharedFlow<RankingEvent> = _events.asSharedFlow()

    init {
        refresh()
    }

    fun onIntent(intent: RankingIntent) {
        when (intent) {
            RankingIntent.Refresh -> refresh()
            RankingIntent.GoHome -> viewModelScope.launch { _events.emit(RankingEvent.NavigateHome) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val entries = deps.rankingRepository.getTodayRankings()
            _uiState.update {
                it.copy(
                    entries = entries,
                    highlightNickname = deps.sessionHolder.highlightNickname,
                    isLoading = false,
                )
            }
        }
    }
}
