package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.data.auth.StaffAuthErrorMapper
import jp.co.yumemi.quiz.droidkaigi.core.data.auth.restoreStaffSessionFromFirebase
import jp.co.yumemi.quiz.droidkaigi.core.data.auth.staffSignInWithEmailPassword
import jp.co.yumemi.quiz.droidkaigi.core.data.auth.staffSignOutFromFirebase
import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FirestoreBootstrap
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthException
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffSession
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAuthRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class ProdStaffAuthRepository(
    private val staffAuthHolder: StaffAuthHolder,
) : StaffAuthRepository {
    override suspend fun signIn(email: String, password: String): Result<StaffSession> =
        try {
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
