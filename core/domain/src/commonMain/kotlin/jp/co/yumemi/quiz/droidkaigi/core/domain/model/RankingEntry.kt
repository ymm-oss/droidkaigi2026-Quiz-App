package jp.co.yumemi.quiz.droidkaigi.core.domain.model

import kotlinx.serialization.Serializable
import kotlin.time.Instant

@Serializable
data class RankingEntry(
    val nickname: String,
    /** Ranking sort key: 0–100 percent accuracy (partial credit for multi / reorder). */
    val score: Int,
    val completedAtEpochMillis: Long,
    /** Firestore document id / fake catalog key. Empty for display-only fixtures. */
    val id: String = "",
    /** Question count of the quiz set when submitted. 0 if unknown (legacy docs). */
    val totalCount: Int = 0,
) {
    val completedAt: Instant get() = Instant.fromEpochMilliseconds(completedAtEpochMillis)
}
