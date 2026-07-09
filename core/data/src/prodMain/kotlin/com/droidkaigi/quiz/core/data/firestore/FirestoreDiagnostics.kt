package com.droidkaigi.quiz.core.data.firestore

/**
 * Firestore 調査用ログ（画面には出さない）。
 * Desktop: `./gradlew :*DesktopApp:run` の標準出力 / Android: Logcat。
 */
internal object FirestoreDiagnostics {
    fun log(tag: String, message: String) {
        println("[Firestore/$tag] $message")
    }

    fun logError(tag: String, message: String, cause: Throwable? = null) {
        val detail = cause?.message?.let { " ($it)" }.orEmpty()
        println("[Firestore/$tag] ERROR: $message$detail")
        cause?.printStackTrace()
    }
}
