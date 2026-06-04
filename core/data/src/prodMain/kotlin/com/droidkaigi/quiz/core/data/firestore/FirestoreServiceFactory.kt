package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.StaffAuthHolder

internal expect fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService
