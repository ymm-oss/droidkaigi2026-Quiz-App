package jp.co.yumemi.quiz.droidkaigi.feature.staff

/**
 * Public URLs staff share with attendees.
 * Desktop always uses the production Hosting origin.
 * Wasm `/staff` uses the current origin so preview channels keep matching the participant app.
 */
object ParticipantAppLinks {
    const val PRODUCTION_WEB_URL = "https://ymm-droidkaigi26.web.app/"
    const val GITHUB_RELEASES_URL =
        "https://github.com/ymm-oss/droidkaigi2026-Quiz-App/releases/latest"
}

fun participantWebAppUrlFromOrigin(origin: String): String {
    val trimmed = origin.trim().trimEnd('/')
    if (trimmed.isEmpty()) return ParticipantAppLinks.PRODUCTION_WEB_URL
    return "$trimmed/"
}

expect fun resolveParticipantWebAppUrl(): String
