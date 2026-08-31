package jp.co.yumemi.quiz.droidkaigi.feature.staff

import kotlinx.browser.window

actual fun resolveParticipantWebAppUrl(): String =
    participantWebAppUrlFromOrigin(window.location.origin)
