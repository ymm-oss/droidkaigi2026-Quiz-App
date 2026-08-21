@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.firebasejs.AuthJs
import com.droidkaigi.quiz.core.data.firebasejs.FirebaseAppJs
import com.droidkaigi.quiz.core.data.firebasejs.FirestoreJs
import com.droidkaigi.quiz.core.data.firebasejs.getAuth
import com.droidkaigi.quiz.core.data.firebasejs.getFirestore
import com.droidkaigi.quiz.core.data.firebasejs.initializeApp
import com.droidkaigi.quiz.core.data.firebasejs.jsonParse
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

/**
 * wasm 用 Firebase アプリのシングルトン。
 * 初期化定数 [FirebaseWebConfig] は google-services.json から Gradle が生成する
 * （`:core:data:generateFirebaseWebConfig`）。
 */
internal object FirebaseJsApp {
    private var app: FirebaseAppJs? = null

    fun ensureInitialized() {
        if (app == null) {
            app = initializeApp(buildOptions())
        }
    }

    val firestore: FirestoreJs
        get() = getFirestore(requireApp())

    val auth: AuthJs
        get() = getAuth(requireApp())

    private fun requireApp(): FirebaseAppJs {
        ensureInitialized()
        return checkNotNull(app)
    }

    private fun buildOptions(): JsAny {
        val optionsJson = buildJsonObject {
            put("apiKey", FirebaseWebConfig.API_KEY)
            put("authDomain", FirebaseWebConfig.AUTH_DOMAIN)
            put("projectId", FirebaseWebConfig.PROJECT_ID)
            put("appId", FirebaseWebConfig.APPLICATION_ID)
        }
        return checkNotNull(jsonParse(optionsJson.toString()))
    }
}
