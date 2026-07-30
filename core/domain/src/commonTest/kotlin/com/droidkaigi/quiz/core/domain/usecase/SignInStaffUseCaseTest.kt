package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import com.droidkaigi.quiz.core.domain.model.StaffQuickSignInCredentials
import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
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

    @Test
    fun quickSignIn_whenAvailable_signsInWithDemoCredentials() {
        val session = StaffSession(email = "staff@droidkaigi.local", displayName = "スタッフ")
        val repository = object : StaffAuthRepository {
            override suspend fun signIn(email: String, password: String): Result<StaffSession> {
                assertEquals("staff@droidkaigi.local", email)
                assertEquals("staff2026", password)
                return Result.success(session)
            }

            override fun quickSignInCredentials(): StaffQuickSignInCredentials =
                StaffQuickSignInCredentials(
                    email = "staff@droidkaigi.local",
                    password = "staff2026",
                )
        }
        val signIn = SignInStaffUseCase(repository, store)
        val quickSignIn = QuickSignInStaffUseCase(repository, signIn)

        assertTrue(quickSignIn.isAvailable)
        val result = runBlocking { quickSignIn() }

        assertTrue(result.isSuccess)
        assertEquals(session, store.currentSession)
    }

    @Test
    fun quickSignIn_whenUnavailable_fails() {
        val repository = object : StaffAuthRepository {
            override suspend fun signIn(email: String, password: String): Result<StaffSession> =
                error("should not sign in")
        }
        val signIn = SignInStaffUseCase(repository, store)
        val quickSignIn = QuickSignInStaffUseCase(repository, signIn)

        assertFalse(quickSignIn.isAvailable)
        val result = runBlocking { quickSignIn() }

        assertTrue(result.isFailure)
        assertNull(store.currentSession)
    }
}
