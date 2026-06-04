package com.droidkaigi.quiz.core.data.dto

import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.ReorderItem
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuizSetDto(
    val id: String,
    val title: String,
    val questions: List<QuestionDto>,
)

@Serializable
data class QuestionDto(
    val type: String,
    val id: String,
    val prompt: String,
    val options: List<ChoiceOptionDto>? = null,
    val correctId: String? = null,
    val correctIds: List<String>? = null,
    val items: List<ReorderItemDto>? = null,
    val correctOrder: List<String>? = null,
)

@Serializable
data class ChoiceOptionDto(val id: String, val label: String)

@Serializable
data class ReorderItemDto(val id: String, val label: String)

fun QuizSetDto.toDomain(): QuizSet = QuizSet(
    id = id,
    title = title,
    questions = questions.map { it.toDomain() },
)

fun QuestionDto.toDomain(): Question = when (type) {
    "single_choice" -> SingleChoice(
        id = id,
        prompt = prompt,
        options = options.orEmpty().map { ChoiceOption(it.id, it.label) },
        correctId = correctId.orEmpty(),
    )
    "multiple_choice" -> MultipleChoice(
        id = id,
        prompt = prompt,
        options = options.orEmpty().map { ChoiceOption(it.id, it.label) },
        correctIds = correctIds.orEmpty().toSet(),
    )
    "reorder" -> Reorder(
        id = id,
        prompt = prompt,
        items = items.orEmpty().map { ReorderItem(it.id, it.label) },
        correctOrder = correctOrder.orEmpty(),
    )
    else -> error("Unknown question type: $type")
}
