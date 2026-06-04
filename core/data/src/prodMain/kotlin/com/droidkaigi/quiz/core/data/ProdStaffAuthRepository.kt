package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Inject
@ContributesBinding(AppScope::class)
class ProdStaffAuthRepository : StaffAuthRepository {
    override suspend fun signIn(email: String, password: String): Result<StaffSession> =
        Result.failure(
            StaffAuthException("スタッフ認証は未設定です（Firebase Authentication を導入してください）"),
        )
}
