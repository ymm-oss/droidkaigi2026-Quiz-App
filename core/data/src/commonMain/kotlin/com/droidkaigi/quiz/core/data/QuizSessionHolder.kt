package com.droidkaigi.quiz.core.data

import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.QuizSession

class QuizSessionHolder {
    var currentSession: QuizSession? = null
    var lastResult: QuizResult? = null
    var highlightNickname: String? = null
    /** Folder used for the in-progress or last completed quiz (ranking scope). */
    var playbackFolderId: String? = null
}
