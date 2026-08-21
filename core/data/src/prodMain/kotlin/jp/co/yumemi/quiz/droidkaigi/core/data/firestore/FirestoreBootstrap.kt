package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import kotlin.concurrent.Volatile

internal object FirestoreBootstrap {
    @Volatile
    private var initialized = false

    fun ensureInitialized() {
        if (initialized) return
        synchronized(this) {
            if (initialized) return
            initializeFirebasePlatform()
            initialized = true
        }
    }
}

internal expect fun initializeFirebasePlatform()
