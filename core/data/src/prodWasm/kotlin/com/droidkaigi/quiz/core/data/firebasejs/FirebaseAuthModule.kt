@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
@file:JsModule("firebase/auth")

package com.droidkaigi.quiz.core.data.firebasejs

import kotlin.js.Promise

internal external fun getAuth(app: FirebaseAppJs): AuthJs

internal external fun signInWithEmailAndPassword(
    auth: AuthJs,
    email: String,
    password: String,
): Promise<UserCredentialJs>

internal external fun signOut(auth: AuthJs): Promise<JsAny?>
