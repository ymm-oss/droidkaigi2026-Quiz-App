package com.droidkaigi.quiz.core.domain.ranking

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotEquals

class RankingEntryIdTest {
    @Test
    fun forSession_isStableForSameInputs() {
        val first = RankingEntryId.forSession("folder-a", "太郎", 1_700_000_000_000L)
        val second = RankingEntryId.forSession("folder-a", "太郎", 1_700_000_000_000L)
        assertEquals(first, second)
    }

    @Test
    fun forSession_differsAcrossSessionsAndParticipants() {
        val base = RankingEntryId.forSession("folder-a", "Alice", 1_700_000_000_000L)
        assertNotEquals(base, RankingEntryId.forSession("folder-a", "Alice", 1_700_000_000_001L))
        assertNotEquals(base, RankingEntryId.forSession("folder-a", "Bob", 1_700_000_000_000L))
        assertNotEquals(base, RankingEntryId.forSession("folder-b", "Alice", 1_700_000_000_000L))
    }

    @Test
    fun forSession_rejectsBlankInputs() {
        assertFailsWith<IllegalArgumentException> {
            RankingEntryId.forSession("", "Alice", 1L)
        }
        assertFailsWith<IllegalArgumentException> {
            RankingEntryId.forSession("folder", "", 1L)
        }
    }
}
