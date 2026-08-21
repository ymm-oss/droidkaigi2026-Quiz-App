package jp.co.yumemi.quiz.droidkaigi.core.domain.auth

class StaffAuthException(
    val reason: StaffAuthFailureReason,
    message: String = reason.userMessage(),
    cause: Throwable? = null,
) : Exception(message, cause) {
    constructor(message: String) : this(
        reason = StaffAuthFailureReason.Unknown,
        message = message,
    )
}
