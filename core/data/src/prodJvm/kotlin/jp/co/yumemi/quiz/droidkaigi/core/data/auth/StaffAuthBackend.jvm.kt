package jp.co.yumemi.quiz.droidkaigi.core.data.auth

import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthException
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthFailureReason
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth

internal actual suspend fun staffSignInWithEmailPassword(email: String, password: String): StaffSignInResult {
    return try {
        Firebase.auth.signInWithEmailAndPassword(email, password)
        val user = Firebase.auth.currentUser
            ?: throw StaffAuthException(StaffAuthFailureReason.Unknown)
        val idToken = user.getIdToken(false)
        StaffSignInResult(
            email = user.email ?: email,
            displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "スタッフ",
            idToken = idToken,
        )
    } catch (e: StaffAuthException) {
        throw e
    } catch (e: Exception) {
        throw StaffAuthErrorMapper.toException(e)
    }
}

internal actual suspend fun staffCurrentIdToken(forceRefresh: Boolean): String? =
    Firebase.auth.currentUser?.getIdToken(forceRefresh)

internal actual suspend fun restoreStaffSessionFromFirebase(): StaffSignInResult? {
    val user = Firebase.auth.currentUser ?: return null
    return try {
        val idToken = user.getIdToken(false)
        StaffSignInResult(
            email = user.email ?: return null,
            displayName = user.displayName?.takeIf { it.isNotBlank() } ?: "スタッフ",
            idToken = idToken,
        )
    } catch (e: Exception) {
        println("[StaffAuth] restoreStaffSessionFromFirebase failed: ${e.message}")
        null
    }
}

internal actual suspend fun staffSignOutFromFirebase() {
    Firebase.auth.signOut()
}
