package jp.co.yumemi.quiz.droidkaigi.feature.quiz.home

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(private val deps: AppDependencies = AppDependencies.shared) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            combine(
                deps.siteStatusHolder.sitePublished,
                deps.siteStatusHolder.observeFailed,
                deps.siteStatusHolder.publishedFolderIds,
            ) { published, failed, _ ->
                published to failed
            }.collect { (published, failed) ->
                _uiState.update {
                    it.copy(
                        sitePublished = published,
                        siteStatusCheckFailed = failed && published == null,
                    )
                }
                if (published == true) {
                    loadPublishedFolders()
                } else if (published == false) {
                    _uiState.update { it.copy(publishedFolders = emptyList(), error = null) }
                }
            }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.NicknameChanged -> _uiState.update { state ->
                state.copy(
                    nickname = intent.value,
                    error = state.error.takeIf { it is HomeError.LoadFailed },
                )
            }

            is HomeIntent.SelectPublishedFolder ->
                _uiState.update { it.copy(selectedFolderId = intent.folderId, error = null) }

            HomeIntent.StartQuiz -> startQuiz()

            HomeIntent.Shown -> {
                _uiState.update { it.copy(isLoading = false) }
            }

            HomeIntent.RetrySiteStatus -> {
                deps.siteStatusHolder.requestRetry()
                if (deps.siteStatusHolder.sitePublished.value == true) {
                    viewModelScope.launch { loadPublishedFolders() }
                }
            }
        }
    }

    private suspend fun loadPublishedFolders() {
        val folders = try {
            deps.listPublishedQuizFoldersUseCase()
        } catch (e: CancellationException) {
            throw e
        } catch (@Suppress("TooGenericExceptionCaught") error: Exception) {
            _uiState.update {
                it.copy(
                    publishedFolders = null,
                    error = HomeError.LoadFailed(error.message),
                )
            }
            return
        }
        _uiState.update { state ->
            val selected = when {
                folders.isEmpty() -> null
                folders.any { it.id == state.selectedFolderId } -> state.selectedFolderId
                folders.size == 1 -> folders.first().id
                else -> state.selectedFolderId?.takeIf { id -> folders.any { it.id == id } }
            }
            state.copy(
                publishedFolders = folders,
                selectedFolderId = selected,
                error = state.error.takeUnless { it is HomeError.LoadFailed },
            )
        }
    }

    private fun startQuiz() {
        if (_uiState.value.isLoading) return
        if (!_uiState.value.isSiteOpen) return
        val nickname = _uiState.value.nickname.trim()
        if (nickname.isEmpty()) {
            _uiState.update { it.copy(error = HomeError.EmptyNickname) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            try {
                if (deps.siteStatusHolder.sitePublished.value != true) {
                    _uiState.update { it.copy(isLoading = false, sitePublished = false) }
                    return@launch
                }
                val folders = deps.listPublishedQuizFoldersUseCase()
                val folderId = resolveStartFolderId(folders) ?: return@launch
                val quizSet = deps.getQuizSetForFolderUseCase(folderId)
                val session = deps.quizEngine.startSession(
                    folderId = folderId,
                    quizSet = quizSet,
                    nickname = nickname,
                    startedAtEpochMillis = deps.instantProvider.nowEpochMillis(),
                )
                deps.sessionHolder.beginSession(session)
                _events.emit(HomeEvent.NavigateToQuiz)
                // 画面遷移までの隙間で開始ボタンが再押下されないよう、
                // この画面が composition から外れるまで isLoading=true を維持する。
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = HomeError.LoadFailed(e.message))
                }
            }
        }
    }

    private fun resolveStartFolderId(folders: List<QuizFolder>): String? {
        val selected = _uiState.value.selectedFolderId?.takeIf { id -> folders.any { it.id == id } }
        val folderId = when {
            folders.isEmpty() -> null
            folders.size == 1 -> folders.first().id
            else -> selected
        }
        if (folderId == null) {
            _uiState.update {
                it.copy(
                    isLoading = false,
                    publishedFolders = folders,
                    selectedFolderId = if (folders.isEmpty()) null else it.selectedFolderId,
                    error = if (folders.isEmpty()) {
                        HomeError.NoPublishedFolders
                    } else {
                        HomeError.NoFolderSelected
                    },
                )
            }
        }
        return folderId
    }
}
