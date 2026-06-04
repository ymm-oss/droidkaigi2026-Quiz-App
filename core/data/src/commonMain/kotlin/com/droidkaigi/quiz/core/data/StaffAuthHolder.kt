package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.usecase.StaffAuthSessionStore

class StaffAuthHolder : StaffAuthSessionStore {
    override var currentSession: StaffSession? = null
}
