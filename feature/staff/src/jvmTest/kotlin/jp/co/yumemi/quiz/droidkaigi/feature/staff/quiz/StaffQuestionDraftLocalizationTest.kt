package jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ChoiceOption
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class StaffQuestionDraftLocalizationTest {
    @Test
    fun domainQuestionRoundTripsEnglishContent() {
        val question = SingleChoice(
            id = "q1",
            prompt = "問題",
            explanationMarkdown = "解説",
            options = listOf(
                ChoiceOption("a", "回答", "Answer"),
                ChoiceOption("b", "不正解", "Incorrect"),
            ),
            correctId = "a",
            promptEn = "Question",
            explanationMarkdownEn = "Explanation",
        )

        assertEquals(question, question.toDraft().toQuestion())
    }

    @Test
    fun itemOperationsKeepJapaneseAndEnglishLabelsTogether() {
        val draft = StaffQuestionDraft(
            id = "q1",
            prompt = "問題",
            items = listOf(
                StaffListItem("a", "回答A", "Answer A"),
                StaffListItem("b", "回答B", "Answer B"),
                StaffListItem("c", "回答C", "Answer C"),
            ),
            correctSingleId = "a",
        )

        val updated = draft
            .updateItemLabelEn("b", "Updated B")
            .moveItemUp("c")
            .removeItem("a")

        assertEquals(listOf("c", "b"), updated.items.map { it.id })
        assertEquals(listOf("Answer C", "Updated B"), updated.items.map { it.labelEn })
        assertEquals("c", updated.correctSingleId)
    }

    @Test
    fun englishOnlyChoiceDoesNotSaveSilently() {
        val draft = StaffQuestionDraft(
            id = "q1",
            prompt = "問題",
            items = listOf(
                StaffListItem("a", "回答A", "Answer A"),
                StaffListItem("b", "", "Answer B"),
            ),
            correctSingleId = "b",
        )

        val error = assertFailsWith<IllegalArgumentException> { draft.toQuestion() }
        assertEquals("日本語の選択肢を入力してください", error.message)
    }

    @Test
    fun blankCorrectChoiceDoesNotReassignOnSave() {
        val draft = StaffQuestionDraft(
            id = "q1",
            prompt = "問題",
            items = listOf(
                StaffListItem("a", "回答A", "Answer A"),
                StaffListItem("b", "回答B", "Answer B"),
                StaffListItem("c", "", ""),
            ),
            correctSingleId = "c",
        )

        val error = assertFailsWith<IllegalArgumentException> { draft.toQuestion() }
        assertEquals("正解を選んでください", error.message)
    }
}
