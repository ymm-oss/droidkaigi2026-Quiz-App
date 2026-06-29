package com.droidkaigi.quiz.feature.quiz.result

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

class ResultViewModel(private val deps: AppDependencies = AppDependencies.shared) : ViewModel() {
    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<ResultEvent>()
    val events: SharedFlow<ResultEvent> = _events.asSharedFlow()

    init {
        deps.sessionHolder.lastResult?.let { result ->
            _uiState.update {
                ResultUiState(
                    nickname = result.nickname,
                    correctCount = result.correctCount,
                    totalCount = result.totalCount,
                    targetScore = result.score,
                )
            }
        }
    }

    fun onIntent(intent: ResultIntent) {
        when (intent) {
            ResultIntent.GoToRanking -> viewModelScope.launch {
                _events.emit(ResultEvent.NavigateToRanking)
            }
        }
    }
}
