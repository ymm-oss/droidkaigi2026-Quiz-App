package jp.co.yumemi.quiz.droidkaigi.core.domain.usecase

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffSession

class GetStaffAuthStateUseCase(private val sessionStore: StaffAuthSessionStore) {
    operator fun invoke(): StaffSession? = sessionStore.currentSession
}
