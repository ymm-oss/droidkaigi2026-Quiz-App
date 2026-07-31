package com.droidkaigi.quiz.core.data.firestore

/**
 * Firestore の複合インデックス不足を検出する。
 *
 * `FAILED_PRECONDITION` はインデックス不足以外にも使われるため、code だけでは判定しない。
 * メッセージがインデックス不足を明示し、code が取れる場合は併せて
 * `FAILED_PRECONDITION` である場合だけフォールバック対象にする。
 */
internal object FirestoreMissingIndexError {
    private const val FAILED_PRECONDITION = "FAILED_PRECONDITION"
    private const val REQUIRES_AN_INDEX = "requires an index"

    fun matchesCodeOrMessage(codeName: String?, message: String?): Boolean {
        val requiresIndex = message.orEmpty().contains(REQUIRES_AN_INDEX, ignoreCase = true)
        if (!requiresIndex) return false
        return codeName == null || codeName.equals(FAILED_PRECONDITION, ignoreCase = true)
    }

    /**
     * @param resolveCodeName プラットフォーム固有例外から code 名を取り出す。
     *   GitLive では `(error as? FirebaseFirestoreException)?.code?.name` を渡す。
     */
    fun matches(error: Throwable, resolveCodeName: (Throwable) -> String? = { null }): Boolean {
        var current: Throwable? = error
        while (current != null) {
            if (matchesCodeOrMessage(resolveCodeName(current), current.message)) {
                return true
            }
            current = current.cause
        }
        return false
    }
}
