package com.droidkaigi.quiz.feature.staff.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidkaigi.quiz.core.data.AppDependencies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StaffRankingViewModel(private val folderId: String, private val deps: AppDependencies = AppDependencies.shared) :
    ViewModel() {
    private val _uiState = MutableStateFlow(StaffRankingUiState())
    val uiState: StateFlow<StaffRankingUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onIntent(intent: StaffRankingIntent) {
        when (intent) {
            StaffRankingIntent.Refresh -> refresh()
            is StaffRankingIntent.DeleteEntry -> deleteEntry(intent.entryId)
            StaffRankingIntent.ClearToday -> clearToday()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { deps.getTodayRankingsUseCase(folderId) }
                .onSuccess { entries ->
                    _uiState.update {
                        it.copy(entries = entries, isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            entries = emptyList(),
                            isLoading = false,
                            errorMessage = error.message ?: "ランキングの読み込みに失敗しました",
                        )
                    }
                }
        }
    }

    private fun deleteEntry(entryId: String) {
        if (entryId.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, errorMessage = null) }
            runCatching { deps.deleteRankingEntryUseCase(folderId, entryId) }
                .onSuccess {
                    val entries = deps.getTodayRankingsUseCase(folderId)
                    _uiState.update {
                        it.copy(entries = entries, isMutating = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isMutating = false,
                            errorMessage = error.message ?: "ランキングの削除に失敗しました",
                        )
                    }
                }
        }
    }

    private fun clearToday() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, errorMessage = null) }
            runCatching { deps.clearTodayRankingsUseCase(folderId) }
                .onSuccess {
                    val entries = deps.getTodayRankingsUseCase(folderId)
                    _uiState.update {
                        it.copy(entries = entries, isMutating = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isMutating = false,
                            errorMessage = error.message ?: "ランキングの一括削除に失敗しました",
                        )
                    }
                }
        }
    }
}
