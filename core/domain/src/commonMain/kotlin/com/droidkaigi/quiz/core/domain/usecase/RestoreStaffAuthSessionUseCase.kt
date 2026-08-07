package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository

class RestoreStaffAuthSessionUseCase(
    private val staffAuthRepository: StaffAuthRepository,
    private val sessionStore: StaffAuthSessionStore,
) {
    suspend operator fun invoke(): StaffSession? {
        sessionStore.currentSession?.let { return it }
        val session = staffAuthRepository.restorePersistedSession() ?: return null
        sessionStore.currentSession = session
        return session
    }
}
