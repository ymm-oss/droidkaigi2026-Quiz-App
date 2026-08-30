package jp.co.yumemi.quiz.droidkaigi.feature.ranking

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.drop
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
    private val userSelectedFolderId = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            @OptIn(ExperimentalCoroutinesApi::class)
            combine(
                listenRetry,
                userSelectedFolderId,
            ) { _, selected -> selected }
                .flatMapLatest { selected ->
                    rankingFolderFlow(selected).distinctUntilChanged().flatMapLatest { folderId ->
                        listenRankings(folderId)
                    }
                }
                .collect { }
        }
    }

    fun onIntent(intent: RankingIntent) {
        when (intent) {
            RankingIntent.Refresh -> listenRetry.update { it + 1 }
            is RankingIntent.SelectFolder -> userSelectedFolderId.value = intent.folderId
            RankingIntent.GoHome -> viewModelScope.launch { _events.emit(RankingEvent.NavigateHome) }
        }
    }

    private fun rankingFolderFlow(selected: String?) = flow {
        val folders = try {
            deps.listPublishedQuizFoldersUseCase()
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            if (playbackFolderId == null && selected.isNullOrBlank()) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = RankingError.LoadFailed(error.message),
                    )
                }
                awaitCancellation()
            }
            emptyList()
        }
        _uiState.update { it.copy(publishedFolders = folders) }

        val initial = resolveListeningFolderId(folders, selected)
        if (initial == null) {
            _uiState.update {
                it.copy(
                    entries = emptyList(),
                    selectedFolderId = null,
                    isLoading = false,
                    error = null,
                )
            }
            awaitCancellation()
        }
        emit(initial)
        followPublishedFolderIds(selected)
    }

    private suspend fun FlowCollector<String>.followPublishedFolderIds(selected: String?) {
        val keepCurrentFolder = selected != null || playbackFolderId != null
        deps.siteStatusHolder.publishedFolderIds.drop(1).collect {
            val refreshed = try {
                deps.listPublishedQuizFoldersUseCase()
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
                return@collect
            }
            _uiState.update { it.copy(publishedFolders = refreshed) }
            if (keepCurrentFolder) return@collect
            val next = resolveListeningFolderId(refreshed, selected)
            if (next != null) emit(next)
        }
    }

    private fun listenRankings(folderId: String) = flow {
        _uiState.update { it.copy(isLoading = true, error = null, selectedFolderId = folderId) }
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
                        selectedFolderId = folderId,
                        highlightNickname = deps.sessionHolder.highlightNickname,
                        isLoading = false,
                        error = null,
                    )
                }
                emit(Unit)
            }
    }

    private suspend fun resolveListeningFolderId(folders: List<QuizFolder>, selected: String?): String? {
        return resolveFolderId(folders, selected)
            ?: selected?.takeIf { it.isNotBlank() }
            ?: deps.siteStatusHolder.activeFolderId.value?.takeIf { it.isNotBlank() }
            ?: runCatching { deps.getActiveQuizFolderIdUseCase() }.getOrNull()?.takeIf { it.isNotBlank() }
    }

    private fun resolveFolderId(folders: List<QuizFolder>, selected: String?): String? {
        val fromSelection = selected?.takeIf { id ->
            folders.any { it.id == id } || id == playbackFolderId
        }
        return fromSelection ?: playbackFolderId ?: folders.firstOrNull()?.id
    }
}
