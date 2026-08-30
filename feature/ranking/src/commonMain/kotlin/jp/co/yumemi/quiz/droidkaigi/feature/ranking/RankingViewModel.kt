package jp.co.yumemi.quiz.droidkaigi.feature.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RankingViewModel(private val deps: AppDependencies = AppDependencies.shared) : ViewModel() {
    private val _uiState = MutableStateFlow(RankingUiState(highlightNickname = deps.sessionHolder.highlightNickname))
    val uiState: StateFlow<RankingUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<RankingEvent>()
    val events: SharedFlow<RankingEvent> = _events.asSharedFlow()

    private val playbackFolderId: String? = deps.sessionHolder.playbackFolderId
    private val listenRetry = MutableStateFlow(0)

    init {
        viewModelScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            listenRetry
                .flatMapLatest {
                    rankingFolderFlow().distinctUntilChanged().flatMapLatest { folderId -> listenRankings(folderId) }
                }
                .collect { }
        }
    }

    fun onIntent(intent: RankingIntent) {
        when (intent) {
            RankingIntent.Refresh -> listenRetry.update { it + 1 }
            RankingIntent.GoHome -> viewModelScope.launch { _events.emit(RankingEvent.NavigateHome) }
        }
    }

    private fun rankingFolderFlow() = flow {
        if (playbackFolderId != null) {
            emit(playbackFolderId)
            return@flow
        }
        val initial = deps.siteStatusHolder.activeFolderId.value?.takeIf { it.isNotBlank() }
            ?: deps.getActiveQuizFolderIdUseCase()
        emit(initial)
        deps.siteStatusHolder.activeFolderId.collect { folderId ->
            if (!folderId.isNullOrBlank()) emit(folderId)
        }
    }

    private fun listenRankings(folderId: String) = flow {
        _uiState.update { it.copy(isLoading = true, error = null) }
        deps.observeTodayRankingsUseCase(folderId)
            .catch { error ->
                if (error is CancellationException) throw error
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = RankingError.LoadFailed(error.message),
                    )
                }
            }
            .collect { entries ->
                _uiState.update {
                    it.copy(
                        entries = entries,
                        highlightNickname = deps.sessionHolder.highlightNickname,
                        isLoading = false,
                        error = null,
                    )
                }
                emit(Unit)
            }
    }
}
