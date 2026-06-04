package com.droidkaigi.quiz.feature.staff.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.ui.components.QuizMarkdownText
import com.droidkaigi.quiz.core.ui.components.QuizTextField
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffQuestionEditorDialog(
    draft: StaffQuestionDraft,
    isNew: Boolean,
    onDraftChange: (StaffQuestionDraft) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
) {
    var typeMenuExpanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isNew) "問題を追加" else "問題を編集") },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium),
            ) {
                Text(
                    text = "問題 ID: ${draft.id}（自動付与）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = draft.type.label,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("問題形式") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                    )
                    ExposedDropdownMenu(
                        expanded = typeMenuExpanded,
                        onDismissRequest = { typeMenuExpanded = false },
                    ) {
                        StaffQuestionType.entries.forEach { type ->
                            DropdownMenuItem(
                                text = { Text(type.label) },
                                onClick = {
                                    typeMenuExpanded = false
                                    onDraftChange(draft.withTypeChanged(type))
                                },
                            )
                        }
                    }
                }
                QuizTextField(
                    value = draft.prompt,
                    onValueChange = { onDraftChange(draft.copy(prompt = it)) },
                    label = "問題文",
                    singleLine = false,
                )
                StaffChoiceListEditor(
                    draft = draft,
                    onDraftChange = onDraftChange,
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
