package jp.co.yumemi.quiz.droidkaigi.feature.staff.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StaffRankingViewModel(private val folderId: String, private val deps: AppDependencies = AppDependencies.shared) :
    ViewModel() {
    private val _uiState = MutableStateFlow(StaffRankingUiState())
    val uiState: StateFlow<StaffRankingUiState> = _uiState.asStateFlow()

    private var dataGeneration = 0

    init {
        refresh()
    }

    fun onIntent(intent: StaffRankingIntent) {
        when (intent) {
            StaffRankingIntent.Refresh -> refresh()

            StaffRankingIntent.ClearMutationFeedback ->
                _uiState.update { it.copy(mutationError = null, reloadWarning = null) }

            is StaffRankingIntent.DeleteEntry -> deleteEntry(intent.entryId)

            StaffRankingIntent.ClearToday -> clearToday()
        }
    }

    private fun refresh() {
        val generation = ++dataGeneration
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, loadError = null, reloadWarning = null) }
            try {
                val entries = deps.getTodayRankingsUseCase(folderId)
                if (generation != dataGeneration) return@launch
                _uiState.update {
                    it.copy(entries = entries, isLoading = false, loadError = null)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                if (generation != dataGeneration) return@launch
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loadError = e.message ?: "ランキングの読み込みに失敗しました",
                    )
                }
            }
        }
    }

    private fun deleteEntry(entryId: String) {
        if (entryId.isBlank()) return
        val generation = ++dataGeneration
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, mutationError = null, reloadWarning = null) }
            try {
                deps.deleteRankingEntryUseCase(folderId, entryId)
                reloadEntriesAfterMutation(
                    generation = generation,
                    optimisticEntries = { current -> current.filterNot { it.id == entryId } },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                if (generation == dataGeneration) {
                    _uiState.update {
                        it.copy(mutationError = e.message ?: "ランキングの削除に失敗しました")
                    }
                }
            } finally {
                if (generation == dataGeneration) {
                    _uiState.update { it.copy(isMutating = false) }
                }
            }
        }
    }

    private fun clearToday() {
        val generation = ++dataGeneration
        viewModelScope.launch {
            _uiState.update { it.copy(isMutating = true, mutationError = null, reloadWarning = null) }
            try {
                deps.clearTodayRankingsUseCase(folderId)
                reloadEntriesAfterMutation(
                    generation = generation,
                    optimisticEntries = { emptyList() },
                )
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                if (generation == dataGeneration) {
                    _uiState.update {
                        it.copy(mutationError = e.message ?: "ランキングの一括削除に失敗しました")
                    }
                }
            } finally {
                if (generation == dataGeneration) {
                    _uiState.update { it.copy(isMutating = false) }
                }
            }
        }
    }

    private suspend fun reloadEntriesAfterMutation(
        generation: Int,
        optimisticEntries: (List<RankingEntry>) -> List<RankingEntry>,
    ) {
        val reloadResult = runCatching { deps.getTodayRankingsUseCase(folderId) }
        if (generation != dataGeneration) return
        reloadResult
            .onSuccess { entries ->
                _uiState.update { it.copy(entries = entries, mutationError = null, reloadWarning = null) }
            }
            .onFailure {
                _uiState.update { state ->
                    state.copy(
                        entries = optimisticEntries(state.entries),
                        mutationError = null,
                        reloadWarning = "操作は完了しましたが、一覧の更新に失敗しました",
                    )
                }
            }
    }
}
