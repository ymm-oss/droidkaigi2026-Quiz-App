package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.usecase.StaffAuthSessionStore

class StaffAuthHolder : StaffAuthSessionStore {
    override var currentSession: StaffSession? = null

    /** Prod Desktop: Firestore REST の Bearer トークン（fake では未使用） */
    var firebaseIdToken: String? = null

    override fun clearSession() {
        currentSession = null
        firebaseIdToken = null
    }
}
