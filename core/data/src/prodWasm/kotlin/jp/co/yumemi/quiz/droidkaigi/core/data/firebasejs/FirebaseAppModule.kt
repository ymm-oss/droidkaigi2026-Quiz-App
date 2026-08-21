@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@file:JsModule("firebase/app")

package jp.co.yumemi.quiz.droidkaigi.core.data.firebasejs

internal external fun initializeApp(options: JsAny): FirebaseAppJs
