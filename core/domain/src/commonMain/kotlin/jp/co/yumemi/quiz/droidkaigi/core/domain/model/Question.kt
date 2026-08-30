package jp.co.yumemi.quiz.droidkaigi.core.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
sealed interface Question {
    val id: String
    val prompt: String
    val explanationMarkdown: String
    val promptEn: String
    val explanationMarkdownEn: String
}

@Serializable
@SerialName("single_choice")
data class SingleChoice(
    override val id: String,
    override val prompt: String,
    override val explanationMarkdown: String = "",
    val options: List<ChoiceOption>,
    val correctId: String,
    override val promptEn: String = "",
    override val explanationMarkdownEn: String = "",
) : Question

@Serializable
@SerialName("multiple_choice")
data class MultipleChoice(
    override val id: String,
    override val prompt: String,
    override val explanationMarkdown: String = "",
    val options: List<ChoiceOption>,
    val correctIds: Set<String>,
    override val promptEn: String = "",
    override val explanationMarkdownEn: String = "",
) : Question

@Serializable
@SerialName("reorder")
data class Reorder(
    override val id: String,
    override val prompt: String,
    override val explanationMarkdown: String = "",
    val items: List<ReorderItem>,
    val correctOrder: List<String>,
    override val promptEn: String = "",
    override val explanationMarkdownEn: String = "",
) : Question

@Serializable
data class ChoiceOption(val id: String, val label: String, val labelEn: String = "")

@Serializable
data class ReorderItem(val id: String, val label: String, val labelEn: String = "")

/**
 * Returns presentation content for [localeTag] while preserving IDs and answer keys.
 *
 * Existing quiz documents only contain the Japanese/default fields. Empty English fields
 * therefore deliberately fall back to those values instead of producing blank questions.
 */
fun Question.localized(localeTag: String): Question {
    if (!localeTag.startsWith("en", ignoreCase = true)) return this
    return when (this) {
        is SingleChoice -> copy(
            prompt = promptEn.ifBlank { prompt },
            explanationMarkdown = explanationMarkdownEn.ifBlank { explanationMarkdown },
            options = options.map { it.copy(label = it.labelEn.ifBlank { it.label }) },
        )

        is MultipleChoice -> copy(
            prompt = promptEn.ifBlank { prompt },
            explanationMarkdown = explanationMarkdownEn.ifBlank { explanationMarkdown },
            options = options.map { it.copy(label = it.labelEn.ifBlank { it.label }) },
        )

        is Reorder -> copy(
            prompt = promptEn.ifBlank { prompt },
            explanationMarkdown = explanationMarkdownEn.ifBlank { explanationMarkdown },
            items = items.map { it.copy(label = it.labelEn.ifBlank { it.label }) },
        )
    }
}
