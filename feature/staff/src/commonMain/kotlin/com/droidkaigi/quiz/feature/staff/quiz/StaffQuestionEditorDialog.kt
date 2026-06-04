package com.droidkaigi.quiz.feature.staff.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.ReorderItem
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.ui.components.QuizMarkdownText
import com.droidkaigi.quiz.core.ui.components.QuizTextField
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

data class StaffQuestionDraft(
    val id: String = "",
    val prompt: String = "",
    val explanationMarkdown: String = "",
    val typeLabel: String = "単一選択",
    val optionsText: String = "a|選択肢A\nb|選択肢B",
    val correctSingleId: String = "a",
    val correctMultipleIds: String = "a,c",
    val reorderItemsText: String = "1|項目1\n2|項目2",
    val correctOrderText: String = "1,2",
)

fun Question.toDraft(): StaffQuestionDraft = when (this) {
    is SingleChoice -> StaffQuestionDraft(
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown,
        typeLabel = "単一選択",
        optionsText = options.joinToString("\n") { "${it.id}|${it.label}" },
        correctSingleId = correctId,
    )
    is MultipleChoice -> StaffQuestionDraft(
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown,
        typeLabel = "複数選択",
        optionsText = options.joinToString("\n") { "${it.id}|${it.label}" },
        correctMultipleIds = correctIds.joinToString(","),
    )
    is Reorder -> StaffQuestionDraft(
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown,
        typeLabel = "並び替え",
        reorderItemsText = items.joinToString("\n") { "${it.id}|${it.label}" },
        correctOrderText = correctOrder.joinToString(","),
    )
}

fun StaffQuestionDraft.toQuestion(): Question {
    val parsedOptions = parseIdLabelLines(optionsText)
    return when (typeLabel) {
        "複数選択" -> MultipleChoice(
            id = id.trim(),
            prompt = prompt.trim(),
            explanationMarkdown = explanationMarkdown,
            options = parsedOptions,
            correctIds = correctMultipleIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toSet(),
        )
        "並び替え" -> {
            val items = parseIdLabelLines(reorderItemsText).map { ReorderItem(it.id, it.label) }
            Reorder(
                id = id.trim(),
                prompt = prompt.trim(),
                explanationMarkdown = explanationMarkdown,
                items = items,
                correctOrder = correctOrderText.split(",").map { it.trim() }.filter { it.isNotEmpty() },
            )
        }
        else -> SingleChoice(
            id = id.trim(),
            prompt = prompt.trim(),
            explanationMarkdown = explanationMarkdown,
            options = parsedOptions.ifEmpty {
                listOf(ChoiceOption("a", "選択肢A"), ChoiceOption("b", "選択肢B"))
            },
            correctId = correctSingleId.trim().ifEmpty { "a" },
        )
    }
}

private fun parseIdLabelLines(text: String): List<ChoiceOption> =
    text.lines()
        .mapNotNull { line ->
            val parts = line.split("|", limit = 2)
            if (parts.size == 2) ChoiceOption(parts[0].trim(), parts[1].trim()) else null
        }

@Composable
fun StaffQuestionEditorDialog(
    draft: StaffQuestionDraft,
    isNew: Boolean,
    onDraftChange: (StaffQuestionDraft) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "問題を追加" else "問題を編集") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium),
            ) {
                QuizTextField(draft.id, { onDraftChange(draft.copy(id = it)) }, "ID")
                QuizTextField(
                    value = draft.typeLabel,
                    onValueChange = { value ->
                        val allowed = listOf("単一選択", "複数選択", "並び替え")
                        if (value in allowed) onDraftChange(draft.copy(typeLabel = value))
                    },
                    label = "形式（単一選択 / 複数選択 / 並び替え）",
                )
                QuizTextField(
                    value = draft.prompt,
                    onValueChange = { onDraftChange(draft.copy(prompt = it)) },
                    label = "問題文",
                    singleLine = false,
                )
                QuizTextField(
                    value = draft.explanationMarkdown,
                    onValueChange = { onDraftChange(draft.copy(explanationMarkdown = it)) },
                    label = "解説（Markdown）",
                    singleLine = false,
                )
                if (draft.explanationMarkdown.isNotBlank()) {
                    Text("プレビュー", style = MaterialTheme.typography.labelLarge)
                    QuizMarkdownText(draft.explanationMarkdown)
                }
                when (draft.typeLabel) {
                    "並び替え" -> {
                        QuizTextField(
                            value = draft.reorderItemsText,
                            onValueChange = { onDraftChange(draft.copy(reorderItemsText = it)) },
                            label = "項目（id|ラベル、改行区切り）",
                            singleLine = false,
                        )
                        QuizTextField(draft.correctOrderText, { onDraftChange(draft.copy(correctOrderText = it)) }, "正しい順序（idをカンマ区切り）")
                    }
                    "複数選択" -> {
                        QuizTextField(
                            value = draft.optionsText,
                            onValueChange = { onDraftChange(draft.copy(optionsText = it)) },
                            label = "選択肢（id|ラベル、改行区切り）",
                            singleLine = false,
                        )
                        QuizTextField(draft.correctMultipleIds, { onDraftChange(draft.copy(correctMultipleIds = it)) }, "正解 ID（カンマ区切り）")
                    }
                    else -> {
                        QuizTextField(
                            value = draft.optionsText,
                            onValueChange = { onDraftChange(draft.copy(optionsText = it)) },
                            label = "選択肢（id|ラベル、改行区切り）",
                            singleLine = false,
                        )
                        QuizTextField(draft.correctSingleId, { onDraftChange(draft.copy(correctSingleId = it)) }, "正解 ID")
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onSave) { Text("保存") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("キャンセル") }
        },
    )
}
