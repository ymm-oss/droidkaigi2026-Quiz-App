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

/**
 * Ranking row label for completion time.
 * Missing / invalid records (`<= 0`, e.g. Firestore default) show as 「不明」.
 */
fun formatCompletedAtLabel(
    epochMillis: Long,
    timeZone: TimeZone = TimeZone.currentSystemDefault(),
): String {
    if (epochMillis <= 0L) return "不明"
    return formatClockHm(epochMillis, timeZone)
}
