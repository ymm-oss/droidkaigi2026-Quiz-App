package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.auth.StaffAuthErrorMapper
import com.droidkaigi.quiz.core.data.auth.restoreStaffSessionFromFirebase
import com.droidkaigi.quiz.core.data.auth.staffSignInWithEmailPassword
import com.droidkaigi.quiz.core.data.auth.staffSignOutFromFirebase
import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.data.firestore.FirestoreBootstrap
import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class ProdStaffAuthRepository(private val staffAuthHolder: StaffAuthHolder) : StaffAuthRepository {
    override suspend fun signIn(email: String, password: String): Result<StaffSession> = try {
        val result = staffSignInWithEmailPassword(email, password)
        staffAuthHolder.firebaseIdToken = result.idToken
        Result.success(
            StaffSession(
                email = result.email,
                displayName = result.displayName,
            ),
        )
    } catch (e: StaffAuthException) {
        Result.failure(e)
    } catch (e: Exception) {
        Result.failure(StaffAuthErrorMapper.toException(e))
    }

    override suspend fun restorePersistedSession(): StaffSession? {
        FirestoreBootstrap.ensureInitialized()
        val result = restoreStaffSessionFromFirebase() ?: return null
        staffAuthHolder.firebaseIdToken = result.idToken
        return StaffSession(
            email = result.email,
            displayName = result.displayName,
        )
    }

    override suspend fun signOut() {
        FirestoreBootstrap.ensureInitialized()
        staffSignOutFromFirebase()
    }
}
