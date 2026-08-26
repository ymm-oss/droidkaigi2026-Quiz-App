package jp.co.yumemi.quiz.droidkaigi.feature.quiz.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

/** 受付状況の取得がハングした場合に失敗として扱うまでの時間。 */
private const val SITE_PUBLISHED_TIMEOUT_MS = 15_000L

class HomeViewModel(private val deps: AppDependencies = AppDependencies.shared) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    private var siteStatusJob: Job? = null
    private var siteStatusGeneration: Int = 0

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.NicknameChanged -> _uiState.update { it.copy(nickname = intent.value, error = null) }

            is HomeIntent.SelectPublishedFolder ->
                _uiState.update { it.copy(selectedFolderId = intent.folderId, error = null) }

            HomeIntent.StartQuiz -> startQuiz()

            HomeIntent.Shown -> {
                _uiState.update { it.copy(isLoading = false) }
                refreshSitePublished()
            }

            HomeIntent.RetrySiteStatus -> refreshSitePublished()
        }
    }

    private fun refreshSitePublished() {
        // 再入時に旧リクエスト（特に 15 秒タイムアウト）が後勝ちで成功状態を上書きしない。
        siteStatusJob?.cancel()
        val generation = ++siteStatusGeneration
        siteStatusJob = viewModelScope.launch {
            // 既に受付状態が分かっている再チェックでは UI をローディングに戻さない。
            // 失敗からの再試行（siteStatusCheckFailed）や初回（null）だけ null にしてローディング表示する。
            _uiState.update {
                it.copy(
                    sitePublished = if (it.siteStatusCheckFailed) null else it.sitePublished,
                    siteStatusCheckFailed = false,
                )
            }
            // 取得失敗を「受付前（false）」に丸めない。ネットワーク障害とスタッフによる
            // 非公開を区別し、失敗はエラー表示 + 再試行導線にする（SPEC: 失敗時はエラー表示）。
            val published = try {
                withTimeout(SITE_PUBLISHED_TIMEOUT_MS) { deps.getSitePublishedUseCase() }
            } catch (_: TimeoutCancellationException) {
                null
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
                null
            }
            if (generation != siteStatusGeneration) return@launch
            val folders = if (published == true) {
                try {
                    deps.listPublishedQuizFoldersUseCase()
                } catch (e: CancellationException) {
                    throw e
                } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
                    if (generation != siteStatusGeneration) return@launch
                    _uiState.update {
                        it.copy(sitePublished = null, siteStatusCheckFailed = true)
                    }
                    return@launch
                }
            } else {
                emptyList()
            }
            if (generation != siteStatusGeneration) return@launch
            _uiState.update { state ->
                val selected = when {
                    folders.isEmpty() -> null
                    folders.any { it.id == state.selectedFolderId } -> state.selectedFolderId
                    folders.size == 1 -> folders.first().id
                    else -> state.selectedFolderId?.takeIf { id -> folders.any { it.id == id } }
                }
                state.copy(
                    sitePublished = published,
                    siteStatusCheckFailed = published == null,
                    publishedFolders = folders,
                    selectedFolderId = selected,
                )
            }
            deps.siteStatusHolder.updateSitePublished(published)
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
                // Re-check at start so a stale open UI after staff unpublish cannot begin a session.
                // 取得失敗はここで丸めず外側の catch で LoadFailed として表示する。
                val published = deps.getSitePublishedUseCase()
                if (!published) {
                    _uiState.update { it.copy(isLoading = false, sitePublished = false) }
                    deps.siteStatusHolder.updateSitePublished(false)
                    return@launch
                }
                val folders = deps.listPublishedQuizFoldersUseCase()
                val folderId = when {
                    folders.isEmpty() -> {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                publishedFolders = folders,
                                selectedFolderId = null,
                                error = HomeError.NoPublishedFolders,
                            )
                        }
                        return@launch
                    }
                    folders.size == 1 -> folders.first().id
                    else -> {
                        val selected = _uiState.value.selectedFolderId
                            ?.takeIf { id -> folders.any { it.id == id } }
                        if (selected == null) {
                            _uiState.update {
                                it.copy(
                                    isLoading = false,
                                    publishedFolders = folders,
                                    error = HomeError.NoFolderSelected,
                                )
                            }
                            return@launch
                        }
                        selected
                    }
                }
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
                // Use cases may surface diverse failures (remote/IO); show message on Home.
                _uiState.update {
                    it.copy(isLoading = false, error = HomeError.LoadFailed(e.message))
                }
            }
        }
    }
}
