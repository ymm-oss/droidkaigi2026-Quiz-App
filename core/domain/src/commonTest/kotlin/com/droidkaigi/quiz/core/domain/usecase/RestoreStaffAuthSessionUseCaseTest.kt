package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.StaffSession
import com.droidkaigi.quiz.core.domain.repository.StaffAuthRepository
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class RestoreStaffAuthSessionUseCaseTest {
    @Test
    fun returnsExistingSessionWithoutCallingRepository() = runBlocking {
        val store = object : StaffAuthSessionStore {
            override var currentSession: StaffSession? =
                StaffSession(email = "staff@example.com", displayName = "Staff")
        }
        var restoreCalled = false
        val repository = object : StaffAuthRepository {
            override suspend fun signIn(email: String, password: String): Result<StaffSession> =
                error("unused")

            override suspend fun restorePersistedSession(): StaffSession? {
                restoreCalled = true
                return null
            }
        }
        val useCase = RestoreStaffAuthSessionUseCase(repository, store)

        val session = useCase()

        assertEquals("staff@example.com", session?.email)
        assertEquals(false, restoreCalled)
    }

    @Test
    fun restoresPersistedSessionIntoStore() = runBlocking {
        val store = object : StaffAuthSessionStore {
            override var currentSession: StaffSession? = null
        }
        val repository = object : StaffAuthRepository {
            override suspend fun signIn(email: String, password: String): Result<StaffSession> =
                error("unused")

            override suspend fun restorePersistedSession(): StaffSession? =
                StaffSession(email = "staff@example.com", displayName = "Staff")
        }
        val useCase = RestoreStaffAuthSessionUseCase(repository, store)

        val session = useCase()

        assertEquals("staff@example.com", session?.email)
        assertEquals("staff@example.com", store.currentSession?.email)
    }

    @Test
    fun returnsNullWhenNoPersistedSession() = runBlocking {
        val store = object : StaffAuthSessionStore {
            override var currentSession: StaffSession? = null
        }
        val repository = object : StaffAuthRepository {
            override suspend fun signIn(email: String, password: String): Result<StaffSession> =
                error("unused")
        }
        val useCase = RestoreStaffAuthSessionUseCase(repository, store)

        assertNull(useCase())
        assertNull(store.currentSession)
    }
}
