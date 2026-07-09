package com.droidkaigi.quiz.feature.quiz.home

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

class HomeViewModel(
    private val deps: AppDependencies = AppDependencies.shared,
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<HomeEvent>()
    val events: SharedFlow<HomeEvent> = _events.asSharedFlow()

    init {
        viewModelScope.launch {
            runCatching { deps.getActiveQuizFolderIdUseCase() }
                .onFailure { e ->
                    if (e is Exception) {
                        _uiState.update { it.copy(errorMessage = userFacingStartError(e)) }
                    }
                }
        }
    }

    fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.NicknameChanged -> _uiState.update { it.copy(nickname = intent.value, errorMessage = null) }
            HomeIntent.StartQuiz -> startQuiz()
        }
    }

    private fun startQuiz() {
        val nickname = _uiState.value.nickname.trim()
        if (nickname.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "ニックネームを入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                val folderId = deps.getActiveQuizFolderIdUseCase()
                val quizSet = deps.getQuizSetForFolderUseCase(folderId)
                deps.sessionHolder.playbackFolderId = folderId
                deps.sessionHolder.currentSession = deps.quizEngine.startSession(
                    folderId = folderId,
                    quizSet = quizSet,
                    nickname = nickname,
                    startedAtEpochMillis = deps.instantProvider.nowEpochMillis(),
                )
                deps.sessionHolder.highlightNickname = nickname
                _events.emit(HomeEvent.NavigateToQuiz)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = userFacingStartError(e)) }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }

    /**
     * UI には短い文言のみ。手順・例外詳細はデバッグログへ（画面には出さない）。
     */
    private fun userFacingStartError(e: Exception): String {
        val detail = e.message?.trim().orEmpty()
        logStartErrorDetail(e)
        return when {
            detail.contains("公開中の問題がありません") ||
                detail.contains("公開中のクイズがありません") ||
                detail.contains("アクティブなフォルダがありません") ||
                detail.contains("Value was null for non-nullable type") ||
                detail.contains("AppConfigFirestoreDocument") ->
                "公開中の問題がありません"

            detail.contains("ConnectException") ||
                detail.contains("Failed to connect") ||
                detail.contains("Unable to resolve host") ->
                "接続できませんでした"

            else -> "読み込みに失敗しました"
        }
    }

    private fun logStartErrorDetail(e: Exception) {
        // 開発時の標準出力 / Logcat 向け。UI には載せない。
        println("[Home/debug] startQuiz failed: ${e.message}")
        e.printStackTrace()
    }
}
