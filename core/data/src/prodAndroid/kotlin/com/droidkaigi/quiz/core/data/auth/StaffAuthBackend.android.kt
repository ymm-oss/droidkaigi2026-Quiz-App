package com.droidkaigi.quiz.core.data.auth

import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

internal actual suspend fun staffSignInWithEmailPassword(email: String, password: String): StaffSignInResult {
    return try {
        Firebase.auth.signInWithEmailAndPassword(email, password)
        val user = Firebase.auth.currentUser
            ?: throw StaffAuthException("ログインに失敗しました")
        val idToken = user.getIdToken(false)
        StaffSignInResult(
            email = user.email ?: email,
            displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "スタッフ",
            idToken = idToken,
        )
    } catch (e: StaffAuthException) {
        throw e
    } catch (e: Exception) {
        throw StaffAuthException(
            e.message?.takeIf { it.isNotBlank() } ?: "メールアドレスまたはパスワードが正しくありません",
        )
    }
}
