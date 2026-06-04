package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

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
            StaffAuthException("メールアドレスまたはパスワードが正しくありません"),
        )
    }

    companion object {
        const val STAFF_EMAIL = "staff@droidkaigi.local"
        const val STAFF_PASSWORD = "staff2026"
    }
}
