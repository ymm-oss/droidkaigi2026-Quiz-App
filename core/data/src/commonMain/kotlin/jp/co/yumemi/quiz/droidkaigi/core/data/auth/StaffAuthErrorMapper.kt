package jp.co.yumemi.quiz.droidkaigi.core.data.auth

import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthException
import jp.co.yumemi.quiz.droidkaigi.core.domain.auth.StaffAuthFailureReason

/**
 * Maps Firebase Auth / Identity Toolkit failures to [StaffAuthException]
 * with operator-facing messages (never raw API payloads).
 */
internal object StaffAuthErrorMapper {
    fun toException(cause: Throwable): StaffAuthException {
        if (cause is StaffAuthException) return cause
        val reason = resolveReason(cause.message)
        return StaffAuthException(reason = reason, cause = cause)
    }

    fun resolveReason(rawMessage: String?): StaffAuthFailureReason {
        val message = rawMessage.orEmpty()
        val upper = message.uppercase()
        return when {
            containsAny(
                upper,
                "INVALID_EMAIL",
                "MALFORMED_EMAIL",
            ) -> StaffAuthFailureReason.InvalidEmail

            containsAny(
                upper,
                "USER_DISABLED",
            ) -> StaffAuthFailureReason.UserDisabled

            containsAny(
                upper,
                "TOO_MANY_ATTEMPTS",
                "TOO_MANY_REQUESTS",
                "TOO_MANY_ATTEMPTS_TRY_LATER",
            ) -> StaffAuthFailureReason.TooManyAttempts

            containsAny(
                upper,
                "NETWORK_REQUEST_FAILED",
                "NETWORK_ERROR",
                "UNAVAILABLE",
                "TIMEOUT",
                "SOCKET",
                "CONNECTION",
            ) -> StaffAuthFailureReason.Network

            containsAny(
                upper,
                "INVALID_LOGIN_CREDENTIALS",
                "INVALID_PASSWORD",
                "WRONG_PASSWORD",
                "EMAIL_NOT_FOUND",
                "USER_NOT_FOUND",
                "INVALID_CREDENTIAL",
            ) -> StaffAuthFailureReason.InvalidCredentials

            else -> StaffAuthFailureReason.Unknown
        }
    }

    private fun containsAny(haystack: String, vararg needles: String): Boolean = needles.any { it in haystack }
}
