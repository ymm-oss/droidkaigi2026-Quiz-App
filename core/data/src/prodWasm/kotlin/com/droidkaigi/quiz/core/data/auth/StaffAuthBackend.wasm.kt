package com.droidkaigi.quiz.core.data.auth

import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import com.droidkaigi.quiz.core.domain.auth.StaffAuthFailureReason

internal actual suspend fun staffSignInWithEmailPassword(email: String, password: String): StaffSignInResult {
    throw StaffAuthException(StaffAuthFailureReason.Unsupported)
}

internal actual suspend fun restoreStaffSessionFromFirebase(): StaffSignInResult? = null

internal actual suspend fun staffSignOutFromFirebase() {}
