package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffSession
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAuthRepository

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
