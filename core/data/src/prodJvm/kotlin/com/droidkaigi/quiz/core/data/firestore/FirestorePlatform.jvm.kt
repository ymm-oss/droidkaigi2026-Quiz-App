package com.droidkaigi.quiz.core.data.firestore

import android.app.Application
import com.droidkaigi.quiz.core.data.StaffAuthHolder
import com.google.firebase.FirebasePlatform
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize

internal actual fun initializeFirebasePlatform() {
    FirebasePlatform.initializeFirebasePlatform(
        object : FirebasePlatform() {
            private val storage = mutableMapOf<String, String>()

            override fun store(key: String, value: String) {
                storage[key] = value
            }

            override fun retrieve(key: String): String? = storage[key]

            override fun clear(key: String) {
                storage.remove(key)
            }

            override fun log(msg: String) {
                println("[Firebase] $msg")
            }
        },
    )
    val config = GoogleServicesLoader.load()
    Firebase.initialize(
        Application(),
        FirebaseOptions(
            applicationId = config.applicationId,
            apiKey = config.apiKey,
            projectId = config.projectId,
        ),
    )
}

internal actual fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService =
    GitLiveFirestoreService()
