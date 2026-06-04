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
                val quizSet = deps.quizRepository.getDefaultQuizSet()
                deps.sessionHolder.currentSession = deps.quizEngine.startSession(
                    quizSet = quizSet,
                    nickname = nickname,
                    startedAtEpochMillis = deps.instantProvider.nowEpochMillis(),
                )
                deps.sessionHolder.highlightNickname = nickname
                _events.emit(HomeEvent.NavigateToQuiz)
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "読み込みに失敗しました") }
            } finally {
                _uiState.update { it.copy(isLoading = false) }
            }
        }
    }
}
