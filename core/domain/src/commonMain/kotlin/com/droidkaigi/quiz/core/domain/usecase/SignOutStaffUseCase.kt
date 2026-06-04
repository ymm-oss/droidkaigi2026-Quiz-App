package com.droidkaigi.quiz.core.domain.usecase

class SignOutStaffUseCase(
    private val sessionStore: StaffAuthSessionStore,
) {
    operator fun invoke() {
        sessionStore.clearSession()
    }
}
