package com.droidkaigi.quiz.feature.staff.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StaffAuthViewModel(
    private val deps: AppDependencies = AppDependencies.shared,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffAuthUiState())
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
        }
    }

    fun onSignOut() {
        deps.signOutStaffUseCase()
        _uiState.update {
            StaffAuthUiState(email = it.email)
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
            deps.signInStaffUseCase(email, password)
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
                        is StaffAuthException -> error.message
                        else -> error.message ?: "ログインに失敗しました"
                    }
                    _uiState.update {
                        it.copy(isLoading = false, errorMessage = message)
                    }
                }
        }
    }
}
