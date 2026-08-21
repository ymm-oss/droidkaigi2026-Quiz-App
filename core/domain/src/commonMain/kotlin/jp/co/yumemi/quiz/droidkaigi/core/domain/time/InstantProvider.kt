package jp.co.yumemi.quiz.droidkaigi.core.domain.time

import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant

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

fun localDateOfEpochMillis(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): LocalDate = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone).date

fun isSameDay(epochMillis: Long, today: LocalDate, timeZone: TimeZone = TimeZone.currentSystemDefault()): Boolean {
    return localDateOfEpochMillis(epochMillis, timeZone) == today
}
