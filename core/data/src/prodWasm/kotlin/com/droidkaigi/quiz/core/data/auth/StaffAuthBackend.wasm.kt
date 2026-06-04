package com.droidkaigi.quiz.core.data.auth

import com.droidkaigi.quiz.core.domain.auth.StaffAuthException

internal actual suspend fun staffSignInWithEmailPassword(email: String, password: String): StaffSignInResult {
    throw StaffAuthException("Wasm の prod ではスタッフ認証に未対応です。Desktop をご利用ください。")
}
