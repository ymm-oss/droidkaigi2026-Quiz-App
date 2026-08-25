package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

/**
 * Firestore 例外をスタッフ UI 向けの短文に変換する。
 * プラットフォーム固有型に依存せずメッセージ文字列で判定する。
 */
object FirestoreErrorMessages {
    fun from(error: Throwable, fallback: String): String {
        val combined = messageChain(error)
        return when {
            combined.contains("Cloud Firestore API has not been used", ignoreCase = true) ||
                combined.contains("firestore.googleapis.com", ignoreCase = true) ->
                "Firestore API が GCP プロジェクトで有効になっていません。Google Cloud Console で Cloud Firestore API を有効にしてください。"
            combined.contains("Missing or insufficient permissions", ignoreCase = true) ->
                "Firestore の権限がありません。ログイン状態とセキュリティルールを確認してください。"
            else -> error.message?.takeIf { it.isNotBlank() } ?: fallback
        }
    }

    private fun messageChain(error: Throwable): String = buildString {
        var current: Throwable? = error
        while (current != null) {
            current.message?.let {
                if (isNotEmpty()) append(' ')
                append(it)
            }
            current = current.cause
        }
    }
}
