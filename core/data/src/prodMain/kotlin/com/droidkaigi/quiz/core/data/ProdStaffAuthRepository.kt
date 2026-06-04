package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.auth.staffSignInWithEmailPassword
import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
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
            Result.failure(
                StaffAuthException(
                    e.message?.takeIf { it.isNotBlank() }
                        ?: "メールアドレスまたはパスワードが正しくありません",
                ),
            )
        }
}
