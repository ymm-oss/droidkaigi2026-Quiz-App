package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

private val lock = Any()

internal actual fun withFirestoreInitLock(block: () -> Unit) {
    synchronized(lock) {
        block()
    }
}
