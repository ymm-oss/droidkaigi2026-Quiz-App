package com.droidkaigi.quiz.core.domain.ranking

/**
 * Deterministic Firestore document ID for a single quiz completion.
 *
 * Same folder + nickname + session start → same ID (safe retry).
 * A new participation gets a new [startedAtEpochMillis], so it does not overwrite
 * another session's ranking row.
 */
object RankingEntryId {
    fun forSession(folderId: String, nickname: String, startedAtEpochMillis: Long): String {
        require(folderId.isNotBlank()) { "folderId must not be blank" }
        require(nickname.isNotBlank()) { "nickname must not be blank" }
        val safeFolder = folderId.replace('/', '_')
        val nickHex = nickname.encodeToByteArray().toHexString()
        return "${safeFolder}_${startedAtEpochMillis}_$nickHex"
    }

    private fun ByteArray.toHexString(): String = joinToString(separator = "") { byte ->
        (byte.toInt() and 0xFF).toString(16).padStart(2, '0')
    }
}
