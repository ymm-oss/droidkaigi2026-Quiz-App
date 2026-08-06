package com.droidkaigi.quiz.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RankingEntry(
    val nickname: String,
    val score: Int,
    val completedAtEpochMillis: Long,
    /** Firestore document id / fake catalog key. Empty for display-only fixtures. */
    val id: String = "",
) {
    val completedAt: Instant get() = Instant.fromEpochMilliseconds(completedAtEpochMillis)
}
