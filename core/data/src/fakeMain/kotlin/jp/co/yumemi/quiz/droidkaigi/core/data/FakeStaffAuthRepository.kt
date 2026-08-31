package jp.co.yumemi.quiz.droidkaigi.core.data

import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthException
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthFailureReason
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffQuickSignInCredentials
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffSession
import jp.co.yumemi.quiz.droidkaigi.core.domain.repository.StaffAuthRepository

@Inject
@ContributesBinding(AppScope::class)
class FakeStaffAuthRepository : StaffAuthRepository {
    override suspend fun signIn(email: String, password: String): Result<StaffSession> {
        if (email == STAFF_EMAIL && password == STAFF_PASSWORD) {
            return Result.success(
                StaffSession(
                    email = email,
                    displayName = "スタッフ",
                ),
            )
        }
        return Result.failure(
            StaffAuthException(StaffAuthFailureReason.InvalidCredentials),
        )
    }

    override fun quickSignInCredentials(): StaffQuickSignInCredentials =
        StaffQuickSignInCredentials(email = STAFF_EMAIL, password = STAFF_PASSWORD)

    companion object {
        const val STAFF_EMAIL = "staff@droidkaigi.local"
        const val STAFF_PASSWORD = "staff2026"
    }
}
