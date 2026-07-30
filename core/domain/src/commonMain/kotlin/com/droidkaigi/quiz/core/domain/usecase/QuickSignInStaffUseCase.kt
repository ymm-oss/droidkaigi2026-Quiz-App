package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository

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
