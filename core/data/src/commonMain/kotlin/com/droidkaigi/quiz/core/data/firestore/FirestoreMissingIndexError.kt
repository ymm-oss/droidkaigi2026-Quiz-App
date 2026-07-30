package com.droidkaigi.quiz.core.data.firestore

/**
 * Firestore の複合インデックス不足を検出する。
 *
 * 優先: 例外の code 名が `FAILED_PRECONDITION`（GitLive / ネイティブの型判定結果）。
 * 次点: メッセージに `FAILED_PRECONDITION` または `requires an index` が含まれる場合
 * （ラップで型が失われたとき向け）。
 */
internal object FirestoreMissingIndexError {
    private const val FAILED_PRECONDITION = "FAILED_PRECONDITION"
    private const val REQUIRES_AN_INDEX = "requires an index"

    fun matchesCodeOrMessage(codeName: String?, message: String?): Boolean {
        if (codeName.equals(FAILED_PRECONDITION, ignoreCase = true)) return true
        val text = message.orEmpty()
        return text.contains(FAILED_PRECONDITION, ignoreCase = true) ||
            text.contains(REQUIRES_AN_INDEX, ignoreCase = true)
    }

    /**
     * @param resolveCodeName プラットフォーム固有例外から code 名を取り出す。
     *   GitLive では `(error as? FirebaseFirestoreException)?.code?.name` を渡す。
     */
    fun matches(
        error: Throwable,
        resolveCodeName: (Throwable) -> String? = { null },
    ): Boolean {
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
