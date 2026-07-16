package com.droidkaigi.quiz.core.domain.time

import kotlinx.datetime.TimeZone
import kotlin.test.Test
import kotlin.test.assertEquals

class TimeFormatTest {
    @Test
    fun formatClockHm_zeroPadsHourAndMinute() {
        // 2026-07-09T01:05:00Z
        val epochMillis = 1_783_559_100_000L
        assertEquals("01:05", formatClockHm(epochMillis, TimeZone.UTC))
    }

    @Test
    fun formatClockHm_afternoon() {
        // 2026-07-09T14:32:00Z
        val epochMillis = 1_783_607_520_000L
        assertEquals("14:32", formatClockHm(epochMillis, TimeZone.UTC))
    }

    @Test
    fun formatDateClockHm_includesDateAndTime() {
        // 2026-07-09T14:32:00Z
        val epochMillis = 1_783_607_520_000L
        assertEquals("07/09 14:32", formatDateClockHm(epochMillis, TimeZone.UTC))
    }

    @Test
    fun formatCompletedAtLabel_formatsValidEpoch() {
        // 2026-07-09T14:32:00Z
        val epochMillis = 1_783_607_520_000L
        assertEquals("07/09 14:32", formatCompletedAtLabel(epochMillis, TimeZone.UTC))
    }

    @Test
    fun formatCompletedAtLabel_missingIsUnknown() {
        assertEquals("不明", formatCompletedAtLabel(0L))
        assertEquals("不明", formatCompletedAtLabel(-1L))
    }
}
