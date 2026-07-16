package com.droidkaigi.quiz.core.domain.time

import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/** Formats epoch millis as local `HH:mm` (zero-padded). */
fun formatClockHm(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone)
    val hour = local.hour.toString().padStart(2, '0')
    val minute = local.minute.toString().padStart(2, '0')
    return "$hour:$minute"
}

/** Formats epoch millis as local `MM/dd HH:mm` (zero-padded). */
fun formatDateClockHm(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    val local = Instant.fromEpochMilliseconds(epochMillis).toLocalDateTime(timeZone)
    val month = local.monthNumber.toString().padStart(2, '0')
    val day = local.dayOfMonth.toString().padStart(2, '0')
    return "$month/$day ${formatClockHm(epochMillis, timeZone)}"
}

/**
 * Ranking row label for completion time.
 * Missing / invalid records (`<= 0`, e.g. Firestore default) show as 「不明」.
 */
fun formatCompletedAtLabel(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    if (epochMillis <= 0L) return "不明"
    return formatDateClockHm(epochMillis, timeZone)
}
