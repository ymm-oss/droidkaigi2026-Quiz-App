package com.droidkaigi.quiz.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Question {
    val id: String
    val prompt: String
    val explanationMarkdown: String
}

@Serializable
@SerialName("single_choice")
data class SingleChoice(
    override val id: String,
    override val prompt: String,
    override val explanationMarkdown: String = "",
    val options: List<ChoiceOption>,
    val correctId: String,
) : Question

@Serializable
@SerialName("multiple_choice")
data class MultipleChoice(
    override val id: String,
    override val prompt: String,
    override val explanationMarkdown: String = "",
    val options: List<ChoiceOption>,
    val correctIds: Set<String>,
) : Question

@Serializable
@SerialName("reorder")
data class Reorder(
    override val id: String,
    override val prompt: String,
    override val explanationMarkdown: String = "",
    val items: List<ReorderItem>,
    val correctOrder: List<String>,
) : Question

@Serializable
data class ChoiceOption(
    val id: String,
    val label: String,
)

@Serializable
data class ReorderItem(
    val id: String,
    val label: String,
)
