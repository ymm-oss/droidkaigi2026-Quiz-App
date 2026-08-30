package jp.co.yumemi.quiz.droidkaigi.feature.staff.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthException
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthFailureReason
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffSession
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout

class StaffAuthViewModel(private val deps: AppDependencies = AppDependencies.shared) : ViewModel() {
    private companion object {
        private const val SIGN_IN_TIMEOUT_MS = 30_000L
    }
    private val _uiState = MutableStateFlow(
        StaffAuthUiState(showQuickSignIn = deps.quickSignInStaffUseCase.isAvailable),
    )
    val uiState: StateFlow<StaffAuthUiState> = _uiState.asStateFlow()

    init {
        deps.getStaffAuthStateUseCase()?.let { session ->
            _uiState.update {
                it.copy(
                    email = session.email,
                    isAuthenticated = true,
                )
            }
        }
        viewModelScope.launch {
            if (_uiState.value.isAuthenticated) return@launch
            _uiState.update { it.copy(isLoading = true) }
            val session = runCatching {
                withTimeout(SIGN_IN_TIMEOUT_MS) {
                    deps.restoreStaffAuthSessionUseCase()
                }
            }.getOrNull()
            _uiState.update {
                if (session != null) {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        email = session.email,
                    )
                } else {
                    it.copy(isLoading = false)
                }
            }
        }
    }

    fun onIntent(intent: StaffAuthIntent) {
        when (intent) {
            is StaffAuthIntent.EmailChanged -> _uiState.update {
                it.copy(email = intent.value, errorMessage = null)
            }

            is StaffAuthIntent.PasswordChanged -> _uiState.update {
                it.copy(password = intent.value, errorMessage = null)
            }

            StaffAuthIntent.SignIn -> signIn()

            StaffAuthIntent.QuickSignIn -> quickSignIn()
        }
    }

    fun onSignOut() {
        viewModelScope.launch {
            deps.signOutStaffUseCase()
            _uiState.update {
                StaffAuthUiState(
                    email = it.email,
                    showQuickSignIn = it.showQuickSignIn,
                )
            }
        }
    }

    private fun signIn() {
        val email = _uiState.value.email.trim()
        val password = _uiState.value.password
        if (email.isBlank() || password.isBlank()) {
            _uiState.update { it.copy(errorMessage = "メールアドレスとパスワードを入力してください") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = runCatching {
                withTimeout(SIGN_IN_TIMEOUT_MS) {
                    deps.signInStaffUseCase(email, password)
                }
            }.getOrElse { Result.failure(it) }
            applySignInResult(result)
        }
    }

    private fun quickSignIn() {
        if (!_uiState.value.showQuickSignIn) return
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val result = runCatching {
                withTimeout(SIGN_IN_TIMEOUT_MS) {
                    deps.quickSignInStaffUseCase()
                }
            }.getOrElse { Result.failure(it) }
            applySignInResult(result)
        }
    }

    private fun applySignInResult(result: Result<StaffSession>) {
        result
            .onSuccess { session ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isAuthenticated = true,
                        email = session.email,
                        password = "",
                        errorMessage = null,
                    )
                }
            }
            .onFailure { error ->
                val message = when (error) {
                    is StaffAuthException -> error.message ?: error.reason.userMessage()

                    is TimeoutCancellationException ->
                        "ログインがタイムアウトしました。ネットワーク接続を確認してください。"

                    else -> StaffAuthFailureReason.Unknown.userMessage()
                }
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = message)
                }
            }
    }
}
