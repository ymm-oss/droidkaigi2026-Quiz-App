package com.droidkaigi.quiz.core.domain.repository

import com.droidkaigi.quiz.core.domain.model.StaffQuickSignInCredentials
import com.droidkaigi.quiz.core.domain.model.StaffSession

interface StaffAuthRepository {
    suspend fun signIn(email: String, password: String): Result<StaffSession>

    /** Prod: restore [StaffSession] from Firebase Auth persistence when the app restarts. */
    suspend fun restorePersistedSession(): StaffSession? = null

    /** Prod: clear Firebase Auth session (fake is a no-op). */
    suspend fun signOut() {}

    /** Fake/dev harness only. Null in prod (no one-click login). */
    fun quickSignInCredentials(): StaffQuickSignInCredentials? = null
}
