package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository

class SignOutStaffUseCase(
    private val sessionStore: StaffAuthSessionStore,
    private val staffAuthRepository: StaffAuthRepository,
) {
    suspend operator fun invoke() {
        staffAuthRepository.signOut()
        sessionStore.clearSession()
    }
}
