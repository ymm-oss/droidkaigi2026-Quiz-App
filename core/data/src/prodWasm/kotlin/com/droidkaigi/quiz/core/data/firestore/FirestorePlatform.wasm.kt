package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.StaffAuthHolder

internal actual fun initializeFirebasePlatform() {
    FirebaseJsApp.ensureInitialized()
}

internal actual fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService =
    FirebaseJsFirestoreService()
