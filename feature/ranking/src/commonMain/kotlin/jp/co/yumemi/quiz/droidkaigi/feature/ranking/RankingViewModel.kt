package jp.co.yumemi.quiz.droidkaigi.feature.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RankingViewModel(private val deps: AppDependencies = AppDependencies.shared) : ViewModel() {
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
            is RankingIntent.SelectFolder -> {
                _uiState.update { it.copy(selectedFolderId = intent.folderId) }
                refresh()
            }
            RankingIntent.GoHome -> viewModelScope.launch { _events.emit(RankingEvent.NavigateHome) }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                val folders = runCatching { deps.listPublishedQuizFoldersUseCase() }.getOrDefault(emptyList())
                val folderId = resolveFolderId(folders, _uiState.value.selectedFolderId)
                val entries = if (folderId == null) {
                    emptyList()
                } else {
                    deps.getTodayRankingsUseCase(folderId)
                }
                _uiState.update {
                    it.copy(
                        entries = entries,
                        publishedFolders = folders,
                        selectedFolderId = folderId,
                        highlightNickname = deps.sessionHolder.highlightNickname,
                        isLoading = false,
                        error = null,
                    )
                }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                // Keep any previously loaded entries so refresh failures do not wipe the list.
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = RankingError.LoadFailed(e.message),
                    )
                }
            }
        }
    }

    private fun resolveFolderId(folders: List<QuizFolder>, selected: String?): String? {
        val playback = deps.sessionHolder.playbackFolderId
        selected?.takeIf { id -> folders.any { it.id == id } }?.let { return it }
        playback?.takeIf { id -> folders.any { it.id == id } || folders.isEmpty() }?.let { return it }
        return folders.firstOrNull()?.id ?: playback
    }
}
