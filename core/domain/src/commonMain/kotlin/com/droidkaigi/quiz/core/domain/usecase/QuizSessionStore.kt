package com.droidkaigi.quiz.core.domain.usecase

import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.QuizSession

/**
 * Process-wide shared quiz play state (screens hand off via this store, not Navigation args).
 *
 * Sync policy:
 * - [currentSession] is the source of truth for in-progress answers / index.
 * - [finishedAtEpochMillis] / [pendingResult] survive ViewModel recreation so timeBonus and
 *   score submit retries stay stable after process-config changes.
 * - [scoreSubmitInFlight] prevents duplicate remote submits under concurrent UI intents.
 * - Call [beginSession] when starting a quiz and [clearPlaySession] when abandoning mid-play.
 * - [lastResult], [highlightNickname], and [playbackFolderId] outlive the play session for
 *   Result / Ranking screens.
 */
interface QuizSessionStore {
    var currentSession: QuizSession?
    var lastResult: QuizResult?
    var highlightNickname: String?
    var playbackFolderId: String?

    /** Epoch millis when the final answer was accepted (feedback wait must not affect timeBonus). */
    var finishedAtEpochMillis: Long?

    /** Scored once at finish; reused on submit retries. */
    var pendingResult: QuizResult?

    /** True while a score upload is in flight. */
    var scoreSubmitInFlight: Boolean

    fun beginSession(session: QuizSession) {
        currentSession = session
        playbackFolderId = session.folderId
        highlightNickname = session.nickname
        finishedAtEpochMillis = null
        pendingResult = null
        scoreSubmitInFlight = false
    }

    fun clearPlaySession() {
        currentSession = null
        finishedAtEpochMillis = null
        pendingResult = null
        scoreSubmitInFlight = false
    }
}
