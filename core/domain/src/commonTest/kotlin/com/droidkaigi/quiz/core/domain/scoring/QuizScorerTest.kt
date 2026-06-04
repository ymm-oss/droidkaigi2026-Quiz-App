package com.droidkaigi.quiz.core.domain.scoring

import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.MultipleChoiceAnswer
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.ReorderAnswer
import com.droidkaigi.quiz.core.domain.model.ReorderItem
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.domain.model.SingleChoiceAnswer
import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuizScorerTest {
    @Test
    fun singleChoice_correct() {
        val q = SingleChoice(
            id = "1",
            prompt = "Pick one",
            options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B")),
            correctId = "a",
        )
        assertTrue(QuizScorer.isCorrect(q, SingleChoiceAnswer("1", "a")))
        assertFalse(QuizScorer.isCorrect(q, SingleChoiceAnswer("1", "b")))
    }

    @Test
    fun multipleChoice_requiresExactSet() {
        val q = MultipleChoice(
            id = "2",
            prompt = "Pick many",
            options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B"), ChoiceOption("c", "C")),
            correctIds = setOf("a", "c"),
        )
        assertTrue(QuizScorer.isCorrect(q, MultipleChoiceAnswer("2", setOf("a", "c"))))
        assertFalse(QuizScorer.isCorrect(q, MultipleChoiceAnswer("2", setOf("a"))))
        assertFalse(QuizScorer.isCorrect(q, MultipleChoiceAnswer("2", setOf("a", "b", "c"))))
    }

    @Test
    fun reorder_requiresExactOrder() {
        val q = Reorder(
            id = "3",
            prompt = "Order",
            items = listOf(ReorderItem("1", "One"), ReorderItem("2", "Two"), ReorderItem("3", "Three")),
            correctOrder = listOf("2", "1", "3"),
        )
        assertTrue(QuizScorer.isCorrect(q, ReorderAnswer("3", listOf("2", "1", "3"))))
        assertFalse(QuizScorer.isCorrect(q, ReorderAnswer("3", listOf("1", "2", "3"))))
    }

    @Test
    fun score_includesTimeBonus() {
        val scoreFast = QuizScorer.calculateScore(3, 3, elapsedMillis = 5_000)
        val scoreSlow = QuizScorer.calculateScore(3, 3, elapsedMillis = 120_000)
        assertTrue(scoreFast > scoreSlow)
    }
}
