package jp.co.yumemi.quiz.droidkaigi.core.data.auth

import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthFailureReason
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

class StaffAuthErrorMapperTest {
    @Test
    fun resolveReason_mapsInvalidLoginCredentials() {
        val raw =
            """accounts API returned an error. URL: https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=xxx POST Body: Response: { "error": { "code": 400, "message": "INVALID_LOGIN_CREDENTIALS" } }"""
        assertEquals(
            StaffAuthFailureReason.InvalidCredentials,
            StaffAuthErrorMapper.resolveReason(raw),
        )
    }

    @Test
    fun resolveReason_mapsLegacyPasswordErrors() {
        assertEquals(
            StaffAuthFailureReason.InvalidCredentials,
            StaffAuthErrorMapper.resolveReason("INVALID_PASSWORD"),
        )
        assertEquals(
            StaffAuthFailureReason.InvalidCredentials,
            StaffAuthErrorMapper.resolveReason("EMAIL_NOT_FOUND"),
        )
    }

    @Test
    fun resolveReason_mapsInvalidEmail() {
        assertEquals(
            StaffAuthFailureReason.InvalidEmail,
            StaffAuthErrorMapper.resolveReason("INVALID_EMAIL"),
        )
    }

    @Test
    fun resolveReason_mapsUserDisabled() {
        assertEquals(
            StaffAuthFailureReason.UserDisabled,
            StaffAuthErrorMapper.resolveReason("USER_DISABLED"),
        )
    }

    @Test
    fun resolveReason_mapsTooManyAttempts() {
        assertEquals(
            StaffAuthFailureReason.TooManyAttempts,
            StaffAuthErrorMapper.resolveReason("TOO_MANY_ATTEMPTS_TRY_LATER"),
        )
    }

    @Test
    fun resolveReason_mapsNetwork() {
        assertEquals(
            StaffAuthFailureReason.Network,
            StaffAuthErrorMapper.resolveReason("NETWORK_REQUEST_FAILED"),
        )
    }

    @Test
    fun resolveReason_unknownDoesNotExposeRawPayload() {
        val raw = "accounts API returned an error with secret key=abc"
        val exception = StaffAuthErrorMapper.toException(RuntimeException(raw))
        assertEquals(StaffAuthFailureReason.Unknown, exception.reason)
        assertEquals(StaffAuthFailureReason.Unknown.userMessage(), exception.message)
        assertFalse(exception.message.orEmpty().contains("accounts API"))
        assertFalse(exception.message.orEmpty().contains("secret"))
    }

    @Test
    fun userMessages_areOperatorFacingJapanese() {
        assertEquals(
            "メールアドレスまたはパスワードが正しくありません",
            StaffAuthFailureReason.InvalidCredentials.userMessage(),
        )
        assertEquals(
            "ログインに失敗しました",
            StaffAuthFailureReason.Unknown.userMessage(),
        )
    }
}
