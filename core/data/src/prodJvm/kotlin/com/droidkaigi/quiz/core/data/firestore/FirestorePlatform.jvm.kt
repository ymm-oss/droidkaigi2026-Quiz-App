package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.StaffAuthHolder

internal actual fun initializeFirebasePlatform() = Unit

internal actual fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService {
    val config = GoogleServicesLoader.load()
    return RestFirestoreService(config, staffAuthHolder)
}
