package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthException
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffSession
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAuthRepository

class QuickSignInStaffUseCase(
    private val staffAuthRepository: StaffAuthRepository,
    private val signInStaffUseCase: SignInStaffUseCase,
) {
    val isAvailable: Boolean
        get() = staffAuthRepository.quickSignInCredentials() != null

    suspend operator fun invoke(): Result<StaffSession> {
        val credentials = staffAuthRepository.quickSignInCredentials()
            ?: return Result.failure(StaffAuthException("クイックログインは利用できません"))
        return signInStaffUseCase(credentials.email, credentials.password)
    }
}
