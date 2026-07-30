package com.droidkaigi.quiz.core.domain.repository

import com.droidkaigi.quiz.core.domain.model.StaffQuickSignInCredentials
import com.droidkaigi.quiz.core.domain.model.StaffSession

interface StaffAuthRepository {
    suspend fun signIn(email: String, password: String): Result<StaffSession>

    /** Fake/dev harness only. Null in prod (no one-click login). */
    fun quickSignInCredentials(): StaffQuickSignInCredentials? = null
}
