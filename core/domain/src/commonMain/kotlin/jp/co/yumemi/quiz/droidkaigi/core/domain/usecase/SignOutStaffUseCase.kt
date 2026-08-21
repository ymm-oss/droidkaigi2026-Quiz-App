package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAuthRepository

class SignOutStaffUseCase(
    private val sessionStore: StaffAuthSessionStore,
    private val staffAuthRepository: StaffAuthRepository,
) {
    suspend operator fun invoke() {
        staffAuthRepository.signOut()
        sessionStore.clearSession()
    }
}
