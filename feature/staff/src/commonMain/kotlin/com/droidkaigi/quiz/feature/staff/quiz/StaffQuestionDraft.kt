package com.droidkaigi.quiz.feature.staff.quiz

import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.ReorderItem
import com.droidkaigi.quiz.core.domain.model.SingleChoice

enum class StaffQuestionType(val label: String) {
    SingleChoice("単一選択"),
    MultipleChoice("複数選択"),
    Reorder("並び替え"),
}

data class StaffListItem(val id: String, val label: String)

data class StaffQuestionDraft(
    val id: String = "",
    val prompt: String = "",
    val explanationMarkdown: String = "",
    val type: StaffQuestionType = StaffQuestionType.SingleChoice,
    val items: List<StaffListItem> = defaultItems(),
    val correctSingleId: String = "",
    val correctMultipleIds: Set<String> = emptySet(),
)

fun defaultItems(): List<StaffListItem> {
    val a = StaffListItem(id = nextAutoItemId(emptyList()), label = "選択肢A")
    val b = StaffListItem(id = nextAutoItemId(listOf(a)), label = "選択肢B")
    return listOf(a, b)
}

fun nextAutoQuestionId(existing: List<Question>): String {
    val used = existing.map { it.id }.toSet()
    var index = 1
    while (true) {
        val candidate = "q$index"
        if (candidate !in used) return candidate
        index++
    }
}

fun nextAutoItemId(existing: List<StaffListItem>): String {
    val used = existing.map { it.id }.toSet()
    var index = 1
    while (true) {
        val candidate = "opt$index"
        if (candidate !in used) return candidate
        index++
    }
}

fun Question.toDraft(): StaffQuestionDraft = when (this) {
    is SingleChoice -> StaffQuestionDraft(
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown,
        type = StaffQuestionType.SingleChoice,
        items = options.map { StaffListItem(it.id, it.label) },
        correctSingleId = correctId,
    )

    is MultipleChoice -> StaffQuestionDraft(
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown,
        type = StaffQuestionType.MultipleChoice,
        items = options.map { StaffListItem(it.id, it.label) },
        correctMultipleIds = correctIds,
    )

    is Reorder -> StaffQuestionDraft(
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown,
        type = StaffQuestionType.Reorder,
        items = items.map { StaffListItem(it.id, it.label) },
    )
}

fun StaffQuestionDraft.toQuestion(): Question {
    require(prompt.isNotBlank()) { "問題文を入力してください" }
    val trimmedItems = items.map { it.copy(label = it.label.trim()) }.filter { it.label.isNotEmpty() }
    require(trimmedItems.size >= 2) { "項目は2つ以上必要です" }

    return when (type) {
        StaffQuestionType.MultipleChoice -> {
            require(correctMultipleIds.isNotEmpty()) { "正解を1つ以上選んでください" }
            MultipleChoice(
                id = id.trim(),
                prompt = prompt.trim(),
                explanationMarkdown = explanationMarkdown,
                options = trimmedItems.map { ChoiceOption(it.id, it.label) },
                correctIds = correctMultipleIds,
            )
        }

        StaffQuestionType.Reorder -> Reorder(
            id = id.trim(),
            prompt = prompt.trim(),
            explanationMarkdown = explanationMarkdown,
            items = trimmedItems.map { ReorderItem(it.id, it.label) },
            correctOrder = trimmedItems.map { it.id },
        )

        StaffQuestionType.SingleChoice -> {
            val correctId = correctSingleId.takeIf { it.isNotEmpty() }
                ?: trimmedItems.first().id
            require(trimmedItems.any { it.id == correctId }) { "正解を選んでください" }
            SingleChoice(
                id = id.trim(),
                prompt = prompt.trim(),
                explanationMarkdown = explanationMarkdown,
                options = trimmedItems.map { ChoiceOption(it.id, it.label) },
                correctId = correctId,
            )
        }
    }
}

fun StaffQuestionDraft.withTypeChanged(newType: StaffQuestionType): StaffQuestionDraft {
    if (type == newType) return this
    val normalizedItems = items.ifEmpty { defaultItems() }
    return when (newType) {
        StaffQuestionType.SingleChoice -> copy(
            type = newType,
            items = normalizedItems,
            correctSingleId = correctSingleId.takeIf { id -> normalizedItems.any { it.id == id } }
                ?: normalizedItems.firstOrNull()?.id.orEmpty(),
            correctMultipleIds = emptySet(),
        )

        StaffQuestionType.MultipleChoice -> copy(
            type = newType,
            items = normalizedItems,
            correctMultipleIds = correctMultipleIds.filter { id -> normalizedItems.any { it.id == id } }.toSet(),
            correctSingleId = "",
        )

        StaffQuestionType.Reorder -> copy(
            type = newType,
            items = normalizedItems,
            correctSingleId = "",
            correctMultipleIds = emptySet(),
        )
    }
}

fun StaffQuestionDraft.addItem(): StaffQuestionDraft {
    val newItem = StaffListItem(id = nextAutoItemId(items), label = "")
    return copy(items = items + newItem)
}

fun StaffQuestionDraft.updateItemLabel(itemId: String, label: String): StaffQuestionDraft =
    copy(items = items.map { if (it.id == itemId) it.copy(label = label) else it })

fun StaffQuestionDraft.removeItem(itemId: String): StaffQuestionDraft {
    val remaining = items.filter { it.id != itemId }
    return copy(
        items = remaining,
        correctSingleId = if (correctSingleId == itemId) remaining.firstOrNull()?.id.orEmpty() else correctSingleId,
        correctMultipleIds = correctMultipleIds - itemId,
    )
}

fun StaffQuestionDraft.moveItemUp(itemId: String): StaffQuestionDraft = reorderItem(itemId, -1)

fun StaffQuestionDraft.moveItemDown(itemId: String): StaffQuestionDraft = reorderItem(itemId, 1)

private fun StaffQuestionDraft.reorderItem(itemId: String, delta: Int): StaffQuestionDraft {
    val index = items.indexOfFirst { it.id == itemId }
    val target = index + delta
    if (index < 0 || target !in items.indices) return this
    val mutable = items.toMutableList()
    val item = mutable.removeAt(index)
    mutable.add(target, item)
    return copy(items = mutable)
}
