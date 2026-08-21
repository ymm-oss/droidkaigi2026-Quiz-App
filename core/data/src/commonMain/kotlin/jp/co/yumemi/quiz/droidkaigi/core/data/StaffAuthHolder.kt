package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffSession
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.StaffAuthSessionStore

class StaffAuthHolder : StaffAuthSessionStore {
    override var currentSession: StaffSession? = null

    /** Prod: スタッフ ID トークン（fake では未使用） */
    var firebaseIdToken: String? = null

    override fun clearSession() {
        currentSession = null
        firebaseIdToken = null
    }
}
