package com.droidkaigi.quiz.core.domain.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class RankingEntry(val nickname: String, val score: Int, val completedAtEpochMillis: Long) {
    val completedAt: Instant get() = Instant.fromEpochMilliseconds(completedAtEpochMillis)
}
