package jp.co.yumemi.quiz.droidkaigi.core.data.dto

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import kotlin.test.Test
import kotlin.test.assertEquals

class QuizSetDtoLocalizationTest {
    @Test
    fun mapsEnglishFieldsToDomain() {
        val dto = QuestionDto(
            type = "single_choice",
            id = "q1",
            prompt = "問題",
            promptEn = "Question",
            explanationMarkdown = "解説",
            explanationMarkdownEn = "Explanation",
            options = listOf(ChoiceOptionDto("a", "回答", "Answer")),
            correctId = "a",
        )

        val question = dto.toDomain() as SingleChoice

        assertEquals("Question", question.promptEn)
        assertEquals("Explanation", question.explanationMarkdownEn)
        assertEquals("Answer", question.options.single().labelEn)
    }

    @Test
    fun missingEnglishFieldsMapToEmptyForLegacyDocuments() {
        val dto = QuestionDto(
            type = "single_choice",
            id = "q1",
            prompt = "問題",
            options = listOf(ChoiceOptionDto("a", "回答")),
            correctId = "a",
        )

        val question = dto.toDomain() as SingleChoice

        assertEquals("", question.promptEn)
        assertEquals("", question.explanationMarkdownEn)
        assertEquals("", question.options.single().labelEn)
    }
}
