package com.droidkaigi.quiz.core.data.auth

internal data class StaffSignInResult(
    val email: String,
    val displayName: String,
    val idToken: String?,
)

internal expect suspend fun staffSignInWithEmailPassword(email: String, password: String): StaffSignInResult
