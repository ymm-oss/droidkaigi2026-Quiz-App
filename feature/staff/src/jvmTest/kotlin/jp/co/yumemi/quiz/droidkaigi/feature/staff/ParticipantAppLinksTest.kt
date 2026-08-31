package jp.co.yumemi.quiz.droidkaigi.feature.staff

import kotlin.test.Test
import kotlin.test.assertEquals

class ParticipantAppLinksTest {
    @Test
    fun origin_emptyFallsBackToProduction() {
        assertEquals(
            ParticipantAppLinks.PRODUCTION_WEB_URL,
            participantWebAppUrlFromOrigin(""),
        )
        assertEquals(
            ParticipantAppLinks.PRODUCTION_WEB_URL,
            participantWebAppUrlFromOrigin("   "),
        )
    }

    @Test
    fun origin_normalizesTrailingSlash() {
        assertEquals(
            "https://preview.web.app/",
            participantWebAppUrlFromOrigin("https://preview.web.app"),
        )
        assertEquals(
            "https://preview.web.app/",
            participantWebAppUrlFromOrigin("https://preview.web.app/"),
        )
    }

    @Test
    fun jvm_usesProductionHostingUrl() {
        assertEquals(ParticipantAppLinks.PRODUCTION_WEB_URL, resolveParticipantWebAppUrl())
    }
}
