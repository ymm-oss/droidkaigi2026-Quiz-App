package jp.co.yumemi.quiz.droidkaigi.core.data.auth

import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthException
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthFailureReason

internal actual suspend fun staffSignInWithEmailPassword(email: String, password: String): StaffSignInResult {
    throw StaffAuthException(StaffAuthFailureReason.Unsupported)
}

internal actual suspend fun staffCurrentIdToken(forceRefresh: Boolean): String? = null

internal actual suspend fun restoreStaffSessionFromFirebase(): StaffSignInResult? = null

internal actual suspend fun staffSignOutFromFirebase() {}
