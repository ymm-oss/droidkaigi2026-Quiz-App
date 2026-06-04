package com.droidkaigi.quiz.feature.staff.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidkaigi.quiz.core.data.AppDependencies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StaffQuizViewModel(
    private val deps: AppDependencies = AppDependencies.shared,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffQuizUiState())
    val uiState: StateFlow<StaffQuizUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onIntent(intent: StaffQuizIntent) {
        when (intent) {
            StaffQuizIntent.Refresh -> refresh()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { deps.getDefaultQuizSetUseCase() }
                .onSuccess { quizSet ->
                    _uiState.update {
                        it.copy(quizSet = quizSet, isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            quizSet = null,
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load quiz set",
                        )
                    }
                }
        }
    }
}
