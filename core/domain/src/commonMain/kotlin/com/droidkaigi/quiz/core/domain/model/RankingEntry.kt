package com.droidkaigi.quiz.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RankingEntry(val nickname: String, val score: Int, val completedAtEpochMillis: Long) {
    val completedAt: Instant get() = Instant.fromEpochMilliseconds(completedAtEpochMillis)
}
