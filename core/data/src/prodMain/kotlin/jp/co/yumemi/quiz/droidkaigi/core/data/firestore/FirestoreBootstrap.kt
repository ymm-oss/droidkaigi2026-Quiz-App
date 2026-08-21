package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import kotlin.concurrent.Volatile

internal object FirestoreBootstrap {
    @Volatile
    private var initialized = false

    fun ensureInitialized() {
        if (initialized) return
        withFirestoreInitLock {
            if (!initialized) {
                initializeFirebasePlatform()
                initialized = true
            }
        }
    }
}

internal expect fun initializeFirebasePlatform()

/** JVM/Android では synchronized、wasm（シングルスレッド）ではそのまま実行する。 */
internal expect fun withFirestoreInitLock(block: () -> Unit)
