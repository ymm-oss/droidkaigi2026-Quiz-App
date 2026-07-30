package com.droidkaigi.quiz.core.data.firestore

import dev.gitlive.firebase.firestore.FirebaseFirestoreException
import dev.gitlive.firebase.firestore.FirestoreExceptionCode

/**
 * GitLive（Android / Desktop JVM）向けのインデックス不足判定。
 *
 * Android では GitLive の [FirebaseFirestoreException] が Google の同名クラスへの typealias、
 * Desktop JVM では firebase-java-sdk が同 API を提供する。いずれも `.code` で型判定できる。
 *
 * 型で `FAILED_PRECONDITION` と分かる場合はそれを優先し、ラップで型が失われたときだけ
 * メッセージ（`FAILED_PRECONDITION` / `requires an index`）にフォールバックする。
 */
internal fun Throwable.isFirestoreMissingCompositeIndexError(): Boolean {
    var current: Throwable? = this
    while (current != null) {
        val firestoreError = current as? FirebaseFirestoreException
        if (firestoreError != null &&
            firestoreError.code == FirestoreExceptionCode.FAILED_PRECONDITION
        ) {
            return true
        }
        current = current.cause
    }
    return FirestoreMissingIndexError.matches(this)
}
