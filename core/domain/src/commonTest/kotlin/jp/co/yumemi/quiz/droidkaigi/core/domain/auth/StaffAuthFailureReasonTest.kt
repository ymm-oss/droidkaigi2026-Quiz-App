package jp.co.yumemi.quiz.droidkaigi.core.domain.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class StaffAuthFailureReasonTest {
    @Test
    fun userMessages_coverAllReasonsWithoutApiDetails() {
        StaffAuthFailureReason.entries.forEach { reason ->
            val message = reason.userMessage()
            assertFalse(message.isBlank())
            assertFalse(message.contains("identitytoolkit", ignoreCase = true))
            assertFalse(message.contains("INVALID_LOGIN", ignoreCase = true))
            assertFalse(message.contains("accounts API", ignoreCase = true))
        }
    }

    @Test
    fun exception_defaultsToReasonUserMessage() {
        val exception = StaffAuthException(StaffAuthFailureReason.InvalidCredentials)
        assertEquals(
            "メールアドレスまたはパスワードが正しくありません",
            exception.message,
        )
        assertEquals(StaffAuthFailureReason.InvalidCredentials, exception.reason)
    }
}
