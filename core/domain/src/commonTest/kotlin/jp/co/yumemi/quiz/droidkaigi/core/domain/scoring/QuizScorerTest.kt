package jp.co.yumemi.quiz.droidkaigi.core.domain.scoring

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ChoiceOption
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoiceAnswer
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Reorder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ReorderAnswer
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ReorderItem
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoiceAnswer
import kotlin.test.Test
import kotlin.test.assertEquals
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
        assertEquals(1.0, QuizScorer.questionAccuracy(q, SingleChoiceAnswer("1", "a")))
        assertEquals(0.0, QuizScorer.questionAccuracy(q, SingleChoiceAnswer("1", "b")))
    }

    @Test
    fun multipleChoice_requiresExactSet_forFullyCorrect() {
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
    fun multipleChoice_partialOverlapRaisesAccuracy() {
        val q = MultipleChoice(
            id = "2",
            prompt = "Pick many",
            options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B"), ChoiceOption("c", "C")),
            correctIds = setOf("a", "c"),
        )
        assertEquals(1.0, QuizScorer.questionAccuracy(q, MultipleChoiceAnswer("2", setOf("a", "c"))))
        assertEquals(0.5, QuizScorer.questionAccuracy(q, MultipleChoiceAnswer("2", setOf("a"))))
        assertEquals(2.0 / 3.0, QuizScorer.questionAccuracy(q, MultipleChoiceAnswer("2", setOf("a", "b", "c"))))
        assertEquals(0.0, QuizScorer.questionAccuracy(q, MultipleChoiceAnswer("2", setOf("b"))))
    }

    @Test
    fun reorder_requiresExactOrder_forFullyCorrect() {
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
    fun reorder_adjacentSwapScoresHigherThanReverse() {
        val q = Reorder(
            id = "3",
            prompt = "Order",
            items = listOf(ReorderItem("a", "A"), ReorderItem("b", "B"), ReorderItem("c", "C")),
            correctOrder = listOf("a", "b", "c"),
        )
        val exact = QuizScorer.questionAccuracy(q, ReorderAnswer("3", listOf("a", "b", "c")))
        val adjacent = QuizScorer.questionAccuracy(q, ReorderAnswer("3", listOf("a", "c", "b")))
        val reverse = QuizScorer.questionAccuracy(q, ReorderAnswer("3", listOf("c", "b", "a")))
        assertEquals(1.0, exact)
        assertEquals(2.0 / 3.0, adjacent)
        assertEquals(0.0, reverse)
        assertTrue(adjacent > reverse)
    }

    @Test
    fun percentScore_averagesQuestionAccuracies() {
        assertEquals(0, QuizScorer.percentScore(emptyList()))
        assertEquals(100, QuizScorer.percentScore(listOf(1.0, 1.0, 1.0)))
        assertEquals(50, QuizScorer.percentScore(listOf(1.0, 0.0)))
        // 1.0 + 0.5 + 2/3 → 72
        assertEquals(72, QuizScorer.percentScore(listOf(1.0, 0.5, 2.0 / 3.0)))
    }
}
