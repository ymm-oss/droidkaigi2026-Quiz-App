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

object QuizScorer {
    /** score = correctCount * 100 + timeBonus (max 50, decreases with elapsed seconds) */
    fun calculateScore(correctCount: Int, totalCount: Int, elapsedMillis: Long): Int {
        val base = correctCount * 100
        val timeBonus = (50 - (elapsedMillis / 1000).toInt()).coerceIn(0, 50)
        return base + timeBonus
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
        val correct = session.quizSet.questions.count { q ->
            isCorrect(q, session.answers[q.id])
        }
        val elapsed = finishedAtEpochMillis - session.startedAtEpochMillis
        return QuizResult(
            nickname = session.nickname,
            correctCount = correct,
            totalCount = session.quizSet.questions.size,
            score = calculateScore(correct, session.quizSet.questions.size, elapsed),
            elapsedMillis = elapsed,
        )
    }

    fun countCorrect(quizSet: QuizSet, answers: Map<String, Answer>): Int =
        quizSet.questions.count { isCorrect(it, answers[it.id]) }
}
