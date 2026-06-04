package com.droidkaigi.quiz.core.domain.model

data class QuizSession(
    val quizSet: QuizSet,
    val nickname: String,
    val currentIndex: Int = 0,
    val answers: Map<String, Answer> = emptyMap(),
    val startedAtEpochMillis: Long,
) {
    val currentQuestion: Question?
        get() = quizSet.questions.getOrNull(currentIndex)

    val isComplete: Boolean
        get() = currentIndex >= quizSet.questions.size

    val progressLabel: String
        get() = "${currentIndex.coerceAtMost(quizSet.questions.size)} / ${quizSet.questions.size}"
}

data class QuizResult(
    val nickname: String,
    val correctCount: Int,
    val totalCount: Int,
    val score: Int,
    val elapsedMillis: Long,
)
