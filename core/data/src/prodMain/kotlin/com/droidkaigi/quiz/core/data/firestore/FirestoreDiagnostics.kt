package com.droidkaigi.quiz.core.data.firestore

/**
 * prod Firestore 調査用。Desktop スタッフアプリは `./gradlew :staffDesktopApp:run` の標準出力に出る。
 * 本番ビルド前に無効化するか、ログレベルで絞る想定。
 */
internal object FirestoreDiagnostics {
    const val ENABLED = true

    fun log(tag: String, message: String) {
        if (!ENABLED) return
        println("[Firestore/$tag] $message")
    }

    fun logError(tag: String, message: String, cause: Throwable? = null) {
        if (!ENABLED) return
        val detail = cause?.message?.let { " ($it)" }.orEmpty()
        println("[Firestore/$tag] ERROR: $message$detail")
        cause?.printStackTrace()
    }
}
