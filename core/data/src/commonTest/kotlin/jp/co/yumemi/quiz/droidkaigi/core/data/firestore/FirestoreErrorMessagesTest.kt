package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import kotlin.test.Test
import kotlin.test.assertEquals

class FirestoreErrorMessagesTest {
    @Test
    fun from_mapsDisabledFirestoreApi() {
        val error = RuntimeException(
            "Cloud Firestore API has not been used in project ymm-droidkaigi26 before or it is disabled.",
        )
        assertEquals(
            "Firestore API が GCP プロジェクトで有効になっていません。Google Cloud Console で Cloud Firestore API を有効にしてください。",
            FirestoreErrorMessages.from(error, "fallback"),
        )
    }

    @Test
    fun from_mapsPermissionDenied() {
        val error = RuntimeException("PERMISSION_DENIED: Missing or insufficient permissions.")
        assertEquals(
            "Firestore の権限がありません。ログイン状態とセキュリティルールを確認してください。",
            FirestoreErrorMessages.from(error, "fallback"),
        )
    }

    @Test
    fun from_doesNotMapGenericFirestoreHostErrors() {
        val error = RuntimeException("Unable to resolve host firestore.googleapis.com")
        assertEquals("Unable to resolve host firestore.googleapis.com", FirestoreErrorMessages.from(error, "fallback"))
    }

    @Test
    fun from_usesFallbackWhenUnknown() {
        assertEquals("fallback", FirestoreErrorMessages.from(RuntimeException(), "fallback"))
    }
}
