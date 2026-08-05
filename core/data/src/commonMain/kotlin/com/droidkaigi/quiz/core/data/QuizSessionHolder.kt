package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.QuizSession
import com.droidkaigi.quiz.core.domain.usecase.QuizSessionStore

/**
 * App-scoped [QuizSessionStore] implementation.
 * Single instance via Metro `@SingleIn` — do not construct ad hoc copies.
 */
class QuizSessionHolder : QuizSessionStore {
    override var currentSession: QuizSession? = null
    override var lastResult: QuizResult? = null
    override var highlightNickname: String? = null
    override var playbackFolderId: String? = null
    override var finishedAtEpochMillis: Long? = null
    override var pendingResult: QuizResult? = null
    override var scoreSubmitInFlight: Boolean = false
}
