package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import android.app.Application
import jp.co.yumemi.quiz.droidkaigi.core.data.StaffAuthHolder
import com.google.firebase.FirebasePlatform
import com.google.firebase.firestore.FirebaseFirestoreSettings
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.FirebaseOptions
import dev.gitlive.firebase.initialize
import dev.gitlive.firebase.firestore.android
import dev.gitlive.firebase.firestore.firestore
import kotlinx.coroutines.Dispatchers

internal actual fun initializeFirebasePlatform() {
    prepareDesktopFirebaseMainDispatcher()
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
    // ローカル永続化を無効化し、失敗した書き込みをキャッシュ成功扱いにしない。
    Firebase.firestore.android.firestoreSettings =
        FirebaseFirestoreSettings.Builder(Firebase.firestore.android.firestoreSettings)
            .setPersistenceEnabled(false)
            .build()
}

internal actual fun createFirestoreService(staffAuthHolder: StaffAuthHolder): FirestoreService =
    GitLiveFirestoreService()

/**
 * GitLive Firebase on JVM uses the Android SDK bridge. Auth / Firestore register listeners on
 * [Dispatchers.Main]. Install Swing Main before [Firebase.initialize].
 */
internal fun prepareDesktopFirebaseMainDispatcher() {
    // Installs Swing Main when kotlinx-coroutines-swing is on the classpath (Desktop apps).
    Dispatchers.Main
}
