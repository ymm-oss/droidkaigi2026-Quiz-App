package com.droidkaigi.quiz.core.domain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

interface InstantProvider {
    fun nowEpochMillis(): Long
}

class SystemInstantProvider : InstantProvider {
    override fun nowEpochMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()
}

fun InstantProvider.todayLocalDate(timeZone: TimeZone = TimeZone.currentSystemDefault()): LocalDate {
    val instant = Instant.fromEpochMilliseconds(nowEpochMillis())
    return instant.toLocalDateTime(timeZone).date
}

fun isSameDay(epochMillis: Long, today: LocalDate, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    val date = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date
    return date == today
}
