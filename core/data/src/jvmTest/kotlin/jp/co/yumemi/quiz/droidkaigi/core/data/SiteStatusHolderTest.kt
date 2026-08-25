package jp.co.yumemi.quiz.droidkaigi.core.data

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class SiteStatusHolderTest {
    @Test
    fun isRankingNavVisible_onlyWhenPublishedTrue() {
        val holder = SiteStatusHolder()
        assertFalse(holder.isRankingNavVisible)

        holder.updateSitePublished(null)
        assertFalse(holder.isRankingNavVisible)

        holder.updateSitePublished(false)
        assertFalse(holder.isRankingNavVisible)

        holder.updateSitePublished(true)
        assertTrue(holder.isRankingNavVisible)
    }
}
