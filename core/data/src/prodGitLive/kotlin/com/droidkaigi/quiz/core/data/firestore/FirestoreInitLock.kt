package com.droidkaigi.quiz.core.data.firestore

private val lock = Any()

internal actual fun withFirestoreInitLock(block: () -> Unit) {
    synchronized(lock) {
        block()
    }
}
