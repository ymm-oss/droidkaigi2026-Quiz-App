package com.droidkaigi.quiz.feature.staff.auth

data class StaffAuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
)

sealed interface StaffAuthIntent {
    data class EmailChanged(val value: String) : StaffAuthIntent
    data class PasswordChanged(val value: String) : StaffAuthIntent
    data object SignIn : StaffAuthIntent
}
