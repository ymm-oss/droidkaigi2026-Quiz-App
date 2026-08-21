package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

internal actual fun withFirestoreInitLock(block: () -> Unit) {
    block()
}
