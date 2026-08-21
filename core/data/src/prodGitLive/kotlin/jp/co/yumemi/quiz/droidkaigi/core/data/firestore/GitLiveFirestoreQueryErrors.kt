package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import dev.gitlive.firebase.firestore.FirebaseFirestoreException

/**
 * GitLive（Android / Desktop JVM）向けのインデックス不足判定。
 *
 * Android では GitLive の [FirebaseFirestoreException] が Google の同名クラスへの typealias、
 * Desktop JVM では firebase-java-sdk が同 API を提供する。いずれも `.code` で型判定できる。
 *
 * `FAILED_PRECONDITION` だけでは対象を広げすぎるため、インデックス不足を示す
 * メッセージも必須とする。ラップで型が失われた場合は cause chain のメッセージを確認する。
 */
internal fun Throwable.isFirestoreMissingCompositeIndexError(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        val firestoreError = current as? FirebaseFirestoreException
        if (firestoreError != null) {
            if (
                FirestoreMissingIndexError.matchesCodeOrMessage(
                    codeName = firestoreError.code.name,
                    message = firestoreError.message,
                )
            ) {
                return true
            }
        }
        current = current.cause
    }
    return FirestoreMissingIndexError.matches(this)
}
