package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository

interface StaffAuthSessionStore {
    var currentSession: StaffSession?
}

class SignInStaffUseCase(
    private val staffAuthRepository: StaffAuthRepository,
    private val sessionStore: StaffAuthSessionStore,
) {
    suspend operator fun invoke(email: String, password: String): Result<StaffSession> =
        staffAuthRepository.signIn(email.trim(), password).onSuccess { session ->
            sessionStore.currentSession = session
        }
}
