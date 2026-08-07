@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@file:JsModule("firebase/app")

package com.droidkaigi.quiz.core.data.firebasejs

internal external fun initializeApp(options: JsAny): FirebaseAppJs
