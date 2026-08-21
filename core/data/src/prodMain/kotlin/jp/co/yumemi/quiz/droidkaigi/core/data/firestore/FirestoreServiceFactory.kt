package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import jp.co.yumemi.quiz.droidkaigi.core.data.StaffAuthHolder

internal expect fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService
