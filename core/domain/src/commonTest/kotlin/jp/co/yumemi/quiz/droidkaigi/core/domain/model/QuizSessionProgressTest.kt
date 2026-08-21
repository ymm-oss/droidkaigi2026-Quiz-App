package jp.co.yumemi.quiz.droidkaigi.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class QuizSessionProgressTest {
    private val questions = listOf(
        SingleChoice(
            id = "1",
            prompt = "Q1",
            options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B")),
            correctId = "a",
        ),
        SingleChoice(
            id = "2",
            prompt = "Q2",
            options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B")),
            correctId = "a",
        ),
        SingleChoice(
            id = "3",
            prompt = "Q3",
            options = listOf(ChoiceOption("a", "A"), ChoiceOption("b", "B")),
            correctId = "a",
        ),
    )

    private fun session(currentIndex: Int, questionList: List<Question> = questions) = QuizSession(
        folderId = "folder",
        quizSet = QuizSet(id = "set", title = "Test", questions = questionList),
        nickname = "tester",
        currentIndex = currentIndex,
        startedAtEpochMillis = 0L,
    )

    @Test
    fun firstQuestion_showsOneBasedLabelAndMatchingFraction() {
        val s = session(currentIndex = 0)
        assertEquals(1, s.displayQuestionNumber)
        assertEquals("1 / 3", s.progressLabel)
        assertEquals(1f / 3f, s.progressFraction)
        assertFalse(s.isComplete)
    }

    @Test
    fun middleQuestion_showsCurrentNumber() {
        val s = session(currentIndex = 1)
        assertEquals(2, s.displayQuestionNumber)
        assertEquals("2 / 3", s.progressLabel)
        assertEquals(2f / 3f, s.progressFraction)
    }

    @Test
    fun lastQuestion_showsFullProgressBeforeSubmit() {
        val s = session(currentIndex = 2)
        assertEquals(3, s.displayQuestionNumber)
        assertEquals("3 / 3", s.progressLabel)
        assertEquals(1f, s.progressFraction)
        assertFalse(s.isComplete)
    }

    @Test
    fun afterLastAnswer_keepsNOfNUntilResult() {
        val s = session(currentIndex = 3)
        assertTrue(s.isComplete)
        assertEquals(3, s.displayQuestionNumber)
        assertEquals("3 / 3", s.progressLabel)
        assertEquals(1f, s.progressFraction)
    }

    @Test
    fun emptyQuiz_showsZeroOfZero() {
        val s = session(currentIndex = 0, questionList = emptyList())
        assertTrue(s.isComplete)
        assertEquals(0, s.displayQuestionNumber)
        assertEquals("0 / 0", s.progressLabel)
        assertEquals(0f, s.progressFraction)
    }

    @Test
    fun singleQuestion_quizShowsOneOfOne() {
        val s = session(currentIndex = 0, questionList = questions.take(1))
        assertEquals("1 / 1", s.progressLabel)
        assertEquals(1f, s.progressFraction)
    }

    @Test
    fun singleQuestion_afterAnswer_staysOneOfOne() {
        val s = session(currentIndex = 1, questionList = questions.take(1))
        assertTrue(s.isComplete)
        assertEquals("1 / 1", s.progressLabel)
        assertEquals(1f, s.progressFraction)
    }
}
