package com.droidkaigi.quiz.core.data.firestore

import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.auth
import dev.gitlive.firebase.firestore.firestore

private const val AUTH_EMULATOR_PORT = 9099
private const val FIRESTORE_EMULATOR_PORT = 8080

internal object FirebaseEmulatorConnector {
    fun connectAfterInitialize() {
        if (!QuizRuntimeFlags.USE_FIREBASE_EMULATOR) return
        val host = firebaseEmulatorHost()
        Firebase.auth.useEmulator(host, AUTH_EMULATOR_PORT)
        Firebase.firestore.useEmulator(host, FIRESTORE_EMULATOR_PORT)
        FirestoreDiagnostics.log(
            "Emulator",
            "Connected to $host (auth=$AUTH_EMULATOR_PORT, firestore=$FIRESTORE_EMULATOR_PORT)",
        )
    }
}

internal expect fun firebaseEmulatorHost(): String
