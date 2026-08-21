package jp.co.yumemi.quiz.droidkaigi.core.domain.model

data class QuizSession(
    val folderId: String,
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

    /**
     * 1-based question number for UI (`1 / N` on the first question).
     * After the last answer ([isComplete]), stays at the total so feedback shows `N / N`.
     * Empty quiz → `0`.
     */
    val displayQuestionNumber: Int
        get() {
            val total = quizSet.questions.size
            if (total == 0) return 0
            if (isComplete) return total
            return currentIndex + 1
        }

    /** `"current / total"` matching [displayQuestionNumber] (e.g. first question → `"1 / 3"`). */
    val progressLabel: String
        get() = "$displayQuestionNumber / ${quizSet.questions.size}"

    /**
     * Progress bar fraction with the same meaning as [progressLabel]:
     * [displayQuestionNumber] / total. Empty quiz → `0f`; last question / complete → `1f`.
     */
    val progressFraction: Float
        get() {
            val total = quizSet.questions.size
            if (total == 0) return 0f
            return displayQuestionNumber.toFloat() / total.toFloat()
        }
}

data class QuizResult(
    val nickname: String,
    val correctCount: Int,
    val totalCount: Int,
    val score: Int,
    val elapsedMillis: Long,
)
