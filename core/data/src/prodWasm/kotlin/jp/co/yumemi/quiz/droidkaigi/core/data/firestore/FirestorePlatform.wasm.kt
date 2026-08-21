package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import jp.co.yumemi.quiz.droidkaigi.core.data.StaffAuthHolder

internal actual fun initializeFirebasePlatform() {
    FirebaseJsApp.ensureInitialized()
}

internal actual fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService =
    FirebaseJsFirestoreService()
