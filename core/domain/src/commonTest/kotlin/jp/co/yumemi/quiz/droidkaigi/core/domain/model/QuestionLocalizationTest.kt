package jp.co.yumemi.quiz.droidkaigi.core.domain.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame

class QuestionLocalizationTest {
    @Test
    fun englishLocaleUsesEnglishContentWithoutChangingAnswerKeys() {
        val question = MultipleChoice(
            id = "q1",
            prompt = "問題",
            explanationMarkdown = "解説",
            options = listOf(
                ChoiceOption("a", "選択肢A", "Option A"),
                ChoiceOption("b", "選択肢B", "Option B"),
            ),
            correctIds = setOf("a"),
            promptEn = "Question",
            explanationMarkdownEn = "Explanation",
        )

        val localized = question.localized("en-US") as MultipleChoice

        assertEquals("Question", localized.prompt)
        assertEquals("Explanation", localized.explanationMarkdown)
        assertEquals(listOf("Option A", "Option B"), localized.options.map { it.label })
        assertEquals(setOf("a"), localized.correctIds)
    }

    @Test
    fun missingEnglishContentFallsBackFieldByField() {
        val question = Reorder(
            id = "q1",
            prompt = "問題",
            explanationMarkdown = "解説",
            items = listOf(
                ReorderItem("a", "項目A", "Item A"),
                ReorderItem("b", "項目B"),
            ),
            correctOrder = listOf("a", "b"),
        )

        val localized = question.localized("en") as Reorder

        assertEquals("問題", localized.prompt)
        assertEquals("解説", localized.explanationMarkdown)
        assertEquals(listOf("Item A", "項目B"), localized.items.map { it.label })
    }

    @Test
    fun nonEnglishLocaleKeepsOriginalQuestion() {
        val question = SingleChoice(
            id = "q1",
            prompt = "問題",
            options = listOf(ChoiceOption("a", "回答", "Answer")),
            correctId = "a",
            promptEn = "Question",
        )

        assertSame(question, question.localized("ja-JP"))
    }
}
