package jp.co.yumemi.quiz.droidkaigi.core.data.dto

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ChoiceOption
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Question
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Reorder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ReorderItem
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import kotlinx.serialization.Serializable

@Serializable
data class QuizSetDto(val id: String, val title: String, val questions: List<QuestionDto>)

@Serializable
data class QuestionDto(
    val type: String = "",
    val id: String = "",
    val prompt: String = "",
    val explanationMarkdown: String? = null,
    val options: List<ChoiceOptionDto>? = null,
    val correctId: String? = null,
    val correctIds: List<String>? = null,
    val items: List<ReorderItemDto>? = null,
    val correctOrder: List<String>? = null,
    val promptEn: String? = null,
    val explanationMarkdownEn: String? = null,
)

@Serializable
data class ChoiceOptionDto(
    val id: String = "",
    val label: String = "",
    val labelEn: String? = null,
)

@Serializable
data class ReorderItemDto(
    val id: String = "",
    val label: String = "",
    val labelEn: String? = null,
)

fun QuizSetDto.toDomain(): QuizSet = QuizSet(
    id = id,
    title = title,
    questions = questions.mapNotNull { it.toDomain() },
)

fun QuestionDto.toDomain(): Question? = when (type) {
    "single_choice" -> SingleChoice(
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown.orEmpty(),
        options = options.orEmpty().map { ChoiceOption(it.id, it.label, it.labelEn.orEmpty()) },
        correctId = correctId.orEmpty(),
        promptEn = promptEn.orEmpty(),
        explanationMarkdownEn = explanationMarkdownEn.orEmpty(),
    )

    "multiple_choice" -> MultipleChoice(
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown.orEmpty(),
        options = options.orEmpty().map { ChoiceOption(it.id, it.label, it.labelEn.orEmpty()) },
        correctIds = correctIds.orEmpty().toSet(),
        promptEn = promptEn.orEmpty(),
        explanationMarkdownEn = explanationMarkdownEn.orEmpty(),
    )

    "reorder" -> Reorder(
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown.orEmpty(),
        items = items.orEmpty().map { ReorderItem(it.id, it.label, it.labelEn.orEmpty()) },
        correctOrder = correctOrder.orEmpty(),
        promptEn = promptEn.orEmpty(),
        explanationMarkdownEn = explanationMarkdownEn.orEmpty(),
    )

    else -> null
}
