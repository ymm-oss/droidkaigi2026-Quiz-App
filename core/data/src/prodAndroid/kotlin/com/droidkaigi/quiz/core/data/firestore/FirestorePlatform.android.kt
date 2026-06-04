package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.StaffAuthHolder

internal actual fun initializeFirebasePlatform() {
    // Android: default app is initialized via google-services.json + Google Services Gradle plugin.
}

internal actual fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService =
    GitLiveFirestoreService()
