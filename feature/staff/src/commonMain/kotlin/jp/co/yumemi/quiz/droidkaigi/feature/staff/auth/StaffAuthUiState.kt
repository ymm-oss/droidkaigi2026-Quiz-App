package jp.co.yumemi.quiz.droidkaigi.feature.staff.auth

data class StaffAuthUiState(
    val email: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val isAuthenticated: Boolean = false,
    /** Fake/dev harness only. Hidden in prod. */
    val showQuickSignIn: Boolean = false,
)

sealed interface StaffAuthIntent {
    data class EmailChanged(val value: String) : StaffAuthIntent
    data class PasswordChanged(val value: String) : StaffAuthIntent
    data object SignIn : StaffAuthIntent
    data object QuickSignIn : StaffAuthIntent
}
