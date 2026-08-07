package com.droidkaigi.quiz.core.data.firestore

internal actual fun withFirestoreInitLock(block: () -> Unit) {
    block()
}
