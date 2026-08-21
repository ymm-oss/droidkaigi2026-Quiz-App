@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package jp.co.yumemi.quiz.droidkaigi.core.data.auth

import jp.co.yumemi.quiz.droidkaigi.core.data.firebasejs.UserCredentialJs
import jp.co.yumemi.quiz.droidkaigi.core.data.firebasejs.UserJs
import jp.co.yumemi.quiz.droidkaigi.core.data.firebasejs.jsErrorCodeOrNull
import jp.co.yumemi.quiz.droidkaigi.core.data.firebasejs.signInWithEmailAndPassword
import jp.co.yumemi.quiz.droidkaigi.core.data.firebasejs.signOut
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FirebaseJsApp
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthException
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthFailureReason
import kotlinx.coroutines.await
import kotlin.coroutines.cancellation.CancellationException
import kotlin.js.JsException

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
    throw StaffAuthException(
        reason = StaffAuthErrorMapper.resolveReason(e.resolveAuthErrorText()),
        cause = e,
    )
}

/**
 * `JsException.message` は generic な文言のことがあるため、FirebaseError の
 * `code`（例: `auth/invalid-credential`）を優先して判定用テキストに含める。
 * JS SDK のコードはハイフン区切りなので `_` 区切りへ正規化する。
 */
private fun Throwable.resolveAuthErrorText(): String? {
    val code = (this as? JsException)?.thrownValue?.let { jsErrorCodeOrNull(it) }
    return listOfNotNull(code, message)
        .joinToString(" ")
        .takeIf { it.isNotBlank() }
        ?.replace('-', '_')
}

internal actual suspend fun staffCurrentIdToken(forceRefresh: Boolean): String? {
    val user = FirebaseJsApp.auth.currentUser ?: return null
    return user.getIdToken(forceRefresh).await<JsString>().toString()
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
