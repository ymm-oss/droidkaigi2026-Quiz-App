@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package jp.co.yumemi.quiz.droidkaigi.core.data.firebasejs

import kotlin.js.Promise

/** Firebase JS SDK（modular API）のオブジェクト型。宣言のみで実体は npm `firebase`。 */
internal external interface FirebaseAppJs : JsAny

internal external interface FirestoreJs : JsAny

internal external interface QueryJs : JsAny

internal external interface CollectionReferenceJs : QueryJs

internal external interface DocumentReferenceJs : JsAny

internal external interface QueryConstraintJs : JsAny

internal external interface DocumentSnapshotJs : JsAny {
    val id: String
    fun exists(): Boolean
    fun data(): JsAny?
}

internal external interface QuerySnapshotJs : JsAny {
    val docs: JsArray<DocumentSnapshotJs>
}

internal external interface AuthJs : JsAny {
    val currentUser: UserJs?

    /** 永続化されたセッションの復元完了を待つ（v10+）。 */
    fun authStateReady(): Promise<JsAny?>
}

internal external interface UserJs : JsAny {
    val email: JsString?
    val displayName: JsString?
    fun getIdToken(forceRefresh: Boolean): Promise<JsString>
}

internal external interface UserCredentialJs : JsAny {
    val user: UserJs
}
