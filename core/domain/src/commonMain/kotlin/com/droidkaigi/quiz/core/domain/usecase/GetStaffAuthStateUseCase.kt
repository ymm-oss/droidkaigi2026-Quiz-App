package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.StaffSession

class GetStaffAuthStateUseCase(private val sessionStore: StaffAuthSessionStore) {
    operator fun invoke(): StaffSession? = sessionStore.currentSession
}
