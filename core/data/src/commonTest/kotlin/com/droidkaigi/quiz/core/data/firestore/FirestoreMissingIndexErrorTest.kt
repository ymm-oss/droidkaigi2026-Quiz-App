package com.droidkaigi.quiz.core.data.firestore

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class FirestoreMissingIndexErrorTest {
    @Test
    fun matchesCodeOrMessage_prefersFailedPreconditionCode() {
        assertTrue(
            FirestoreMissingIndexError.matchesCodeOrMessage(
                codeName = "FAILED_PRECONDITION",
                message = null,
            ),
        )
        assertTrue(
            FirestoreMissingIndexError.matchesCodeOrMessage(
                codeName = "failed_precondition",
                message = "unrelated",
            ),
        )
    }

    @Test
    fun matchesCodeOrMessage_detectsRequiresAnIndexMessage() {
        assertTrue(
            FirestoreMissingIndexError.matchesCodeOrMessage(
                codeName = null,
                message = "The query requires an index. You can create it here: https://console.firebase.google.com/...",
            ),
        )
    }

    @Test
    fun matchesCodeOrMessage_detectsFailedPreconditionInMessage() {
        assertTrue(
            FirestoreMissingIndexError.matchesCodeOrMessage(
                codeName = null,
                message = "FAILED_PRECONDITION: The query requires an index.",
            ),
        )
    }

    @Test
    fun matchesCodeOrMessage_rejectsOtherErrors() {
        assertFalse(
            FirestoreMissingIndexError.matchesCodeOrMessage(
                codeName = "PERMISSION_DENIED",
                message = "Missing or insufficient permissions.",
            ),
        )
        assertFalse(
            FirestoreMissingIndexError.matchesCodeOrMessage(
                codeName = "UNAVAILABLE",
                message = "The service is currently unavailable.",
            ),
        )
        assertFalse(
            FirestoreMissingIndexError.matchesCodeOrMessage(
                codeName = null,
                message = "Network error",
            ),
        )
        assertFalse(FirestoreMissingIndexError.matchesCodeOrMessage(null, null))
    }

    @Test
    fun matches_usesResolvedCodeNameOverMessage() {
        val error = RuntimeException("permission denied")
        assertTrue(
            FirestoreMissingIndexError.matches(error) { "FAILED_PRECONDITION" },
        )
        assertFalse(
            FirestoreMissingIndexError.matches(error) { "PERMISSION_DENIED" },
        )
    }

    @Test
    fun matches_walksCauseChain() {
        val root = RuntimeException(
            "wrapper",
            IllegalStateException("FAILED_PRECONDITION: The query requires an index."),
        )
        assertTrue(FirestoreMissingIndexError.matches(root))
    }

    @Test
    fun matches_doesNotSwallowPermissionOrNetworkErrors() {
        assertFalse(
            FirestoreMissingIndexError.matches(
                RuntimeException("Missing or insufficient permissions."),
            ),
        )
        assertFalse(
            FirestoreMissingIndexError.matches(
                RuntimeException("UNAVAILABLE: connect timed out"),
            ) { "UNAVAILABLE" },
        )
    }
}
