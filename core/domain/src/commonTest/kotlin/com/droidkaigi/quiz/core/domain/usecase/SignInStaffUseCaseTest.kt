package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class SignInStaffUseCaseTest {
    private val store = object : StaffAuthSessionStore {
        override var currentSession: StaffSession? = null
    }

    @Test
    fun signIn_success_storesSession() {
        val session = StaffSession(email = "a@b.c", displayName = "Test")
        val useCase = SignInStaffUseCase(
            staffAuthRepository = object : StaffAuthRepository {
                override suspend fun signIn(email: String, password: String): Result<StaffSession> =
                    Result.success(session)
            },
            sessionStore = store,
        )

        val result = runBlocking { useCase("a@b.c", "secret") }

        assertTrue(result.isSuccess)
        assertEquals(session, store.currentSession)
    }

    @Test
    fun signIn_failure_clearsStore() {
        store.currentSession = StaffSession(email = "old", displayName = "Old")
        val useCase = SignInStaffUseCase(
            staffAuthRepository = object : StaffAuthRepository {
                override suspend fun signIn(email: String, password: String): Result<StaffSession> =
                    Result.failure(StaffAuthException("invalid"))
            },
            sessionStore = store,
        )

        val result = runBlocking { useCase("wrong", "wrong") }

        assertTrue(result.isFailure)
        assertNotNull(store.currentSession)
        assertEquals("old", store.currentSession?.email)
    }

    @Test
    fun getStaffAuthState_whenEmpty_returnsNull() {
        store.currentSession = null
        val useCase = GetStaffAuthStateUseCase(store)
        assertNull(useCase())
    }
}
