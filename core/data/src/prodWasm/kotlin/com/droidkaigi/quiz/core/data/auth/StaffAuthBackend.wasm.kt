@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.droidkaigi.quiz.core.data.auth

import com.droidkaigi.quiz.core.data.firebasejs.UserCredentialJs
import com.droidkaigi.quiz.core.data.firebasejs.UserJs
import com.droidkaigi.quiz.core.data.firebasejs.signInWithEmailAndPassword
import com.droidkaigi.quiz.core.data.firebasejs.signOut
import com.droidkaigi.quiz.core.data.firestore.FirebaseJsApp
import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import com.droidkaigi.quiz.core.domain.auth.StaffAuthFailureReason
import kotlinx.coroutines.await
import kotlin.coroutines.cancellation.CancellationException

// JS 例外は Throwable としてしか捕まえられず、throw 構成は JVM / Android 実装と揃える
@Suppress("TooGenericExceptionCaught", "ThrowsCount")
internal actual suspend fun staffSignInWithEmailPassword(email: String, password: String): StaffSignInResult = try {
    val credential = signInWithEmailAndPassword(FirebaseJsApp.auth, email, password)
        .await<UserCredentialJs>()
    credential.user.toSignInResult(fallbackEmail = email)
        ?: throw StaffAuthException(StaffAuthFailureReason.Unknown)
} catch (e: CancellationException) {
    throw e
} catch (e: StaffAuthException) {
    throw e
} catch (e: Throwable) {
    // JS SDK のエラーコードは `auth/invalid-credential` 形式なので `_` 区切りへ正規化して判定する
    throw StaffAuthException(
        reason = StaffAuthErrorMapper.resolveReason(e.message?.replace('-', '_')),
        cause = e,
    )
}

@Suppress("TooGenericExceptionCaught")
internal actual suspend fun restoreStaffSessionFromFirebase(): StaffSignInResult? {
    val auth = FirebaseJsApp.auth
    return try {
        // ブラウザの永続化セッションが解決されるまで currentUser は null のことがある
        auth.authStateReady().await<JsAny?>()
        auth.currentUser?.toSignInResult(fallbackEmail = null)
    } catch (e: CancellationException) {
        throw e
    } catch (e: Throwable) {
        println("[StaffAuth] restoreStaffSessionFromFirebase failed: ${e.message}")
        null
    }
}

internal actual suspend fun staffSignOutFromFirebase() {
    signOut(FirebaseJsApp.auth).await<JsAny?>()
}

private suspend fun UserJs.toSignInResult(fallbackEmail: String?): StaffSignInResult? {
    val resolvedEmail = email?.toString() ?: fallbackEmail ?: return null
    val idToken = getIdToken(false).await<JsString>().toString()
    return StaffSignInResult(
        email = resolvedEmail,
        displayName = displayName?.toString()?.takeIf { it.isNotBlank() } ?: "スタッフ",
        idToken = idToken,
    )
}
