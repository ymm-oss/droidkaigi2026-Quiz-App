package com.droidkaigi.quiz.core.data.auth

internal data class StaffSignInResult(
    val email: String,
    val displayName: String,
    val idToken: String?,
)

internal expect suspend fun staffSignInWithEmailPassword(email: String, password: String): StaffSignInResult

/** Returns a fresh Firebase ID token for the current staff session, or null if signed out. */
internal expect suspend fun staffCurrentIdToken(forceRefresh: Boolean): String?