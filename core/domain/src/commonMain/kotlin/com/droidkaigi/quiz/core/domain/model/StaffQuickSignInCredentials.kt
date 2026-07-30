package com.droidkaigi.quiz.core.domain.model

/** Fake/dev harness only. Must not be used as a prod credential source. */
data class StaffQuickSignInCredentials(
    val email: String,
    val password: String,
)
