package jp.co.yumemi.quiz.droidkaigi.core.domain.scoring

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Answer
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoiceAnswer
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Question
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSession
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Reorder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ReorderAnswer
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoiceAnswer
import kotlin.math.roundToInt

object QuizScorer {
    /**
     * Average per-question accuracy as 0–100.
     * Multiple choice uses Jaccard overlap; reorder uses pairwise order (Kendall).
     */
    fun percentScore(accuracies: List<Double>): Int {
        if (accuracies.isEmpty()) return 0
        return (accuracies.average() * 100.0).roundToInt().coerceIn(0, 100)
    }

    fun questionAccuracy(question: Question, answer: Answer?): Double {
        if (answer == null || answer.questionId != question.id) return 0.0
        return when (question) {
            is SingleChoice -> {
                val selected = (answer as? SingleChoiceAnswer)?.selectedId
                if (selected == question.correctId) 1.0 else 0.0
            }
            is MultipleChoice -> {
                val selected = (answer as? MultipleChoiceAnswer)?.selectedIds ?: return 0.0
                jaccard(selected, question.correctIds)
            }
            is Reorder -> {
                val ordered = (answer as? ReorderAnswer)?.orderedIds ?: return 0.0
                reorderAccuracy(question.correctOrder, ordered)
            }
        }
    }

    fun isCorrect(question: Question, answer: Answer?): Boolean {
        if (answer == null || answer.questionId != question.id) return false
        return when (question) {
            is SingleChoice -> (answer as? SingleChoiceAnswer)?.selectedId == question.correctId
            is MultipleChoice -> (answer as? MultipleChoiceAnswer)?.selectedIds == question.correctIds
            is Reorder -> (answer as? ReorderAnswer)?.orderedIds == question.correctOrder
        }
    }

    fun scoreSession(session: QuizSession, finishedAtEpochMillis: Long): QuizResult {
        val questions = session.quizSet.questions
        val correct = questions.count { q -> isCorrect(q, session.answers[q.id]) }
        val accuracies = questions.map { q -> questionAccuracy(q, session.answers[q.id]) }
        val elapsed = finishedAtEpochMillis - session.startedAtEpochMillis
        return QuizResult(
            nickname = session.nickname,
            correctCount = correct,
            totalCount = questions.size,
            score = percentScore(accuracies),
            elapsedMillis = elapsed,
        )
    }

    fun countCorrect(quizSet: QuizSet, answers: Map<String, Answer>): Int =
        quizSet.questions.count { isCorrect(it, answers[it.id]) }

    internal fun jaccard(selected: Set<String>, correct: Set<String>): Double {
        if (selected.isEmpty() && correct.isEmpty()) return 1.0
        val unionSize = (selected + correct).size
        if (unionSize == 0) return 1.0
        return selected.intersect(correct).size.toDouble() / unionSize.toDouble()
    }

    internal fun reorderAccuracy(correctOrder: List<String>, orderedIds: List<String>): Double {
        if (correctOrder.isEmpty()) return 0.0
        if (orderedIds == correctOrder) return 1.0
        val rank = correctOrder.withIndex().associate { it.value to it.index }
        val sequence = orderedIds.filter { it in rank }
        val coverage = sequence.size.toDouble() / correctOrder.size.toDouble()
        if (sequence.size < 2) {
            return if (correctOrder.size == 1 && sequence.size == 1) 1.0 else 0.0
        }
        var concordant = 0
        var pairs = 0
        for (i in sequence.indices) {
            for (j in (i + 1) until sequence.size) {
                pairs += 1
                if (rank.getValue(sequence[i]) < rank.getValue(sequence[j])) {
                    concordant += 1
                }
            }
        }
        val orderRatio = if (pairs == 0) 0.0 else concordant.toDouble() / pairs.toDouble()
        return orderRatio * coverage
    }
}
