package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import jp.co.yumemi.quiz.droidkaigi.core.data.StaffAuthHolder

internal actual fun initializeFirebasePlatform() {
    // Android: default app is initialized via google-services.json + Google Services Gradle plugin.
}

internal actual fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService =
    GitLiveFirestoreService()
