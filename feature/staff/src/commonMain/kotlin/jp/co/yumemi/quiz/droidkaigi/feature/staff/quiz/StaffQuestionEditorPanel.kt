package jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizMarkdownText
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffHorizontalDivider
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffTextButton
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.staffDividerColor

/**
 * Right-hand editor panel. A side panel (rather than a dialog) keeps the question list visible
 * while staff edit, which is the whole point of the console during a live session.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffQuestionEditorPanel(
    draft: StaffQuestionDraft,
    isNew: Boolean,
    onDraftChange: (StaffQuestionDraft) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var typeMenuExpanded by remember { mutableStateOf(false) }
    var showPromptPreview by remember { mutableStateOf(false) }
    var showExplanationPreview by remember { mutableStateOf(false) }

    val borderColor = staffDividerColor()
    Column(
        modifier = modifier
            .width(QuizTokens.staffEditorPanelWidth)
            .fillMaxHeight()
            .shadow(16.dp)
            .background(MaterialTheme.colorScheme.surfaceContainerLow)
            .drawBehind {
                drawLine(
                    color = borderColor,
                    start = Offset.Zero,
                    end = Offset(0f, size.height),
                    strokeWidth = 1.dp.toPx(),
                )
            },
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuizTokens.spacingLarge - 4.dp),
            verticalAlignment = Alignment.Top,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (isNew) "問題を追加" else "問題を編集",
                    style = MaterialTheme.typography.titleLarge,
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "問題 ID: ${draft.id}（自動付与）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "閉じる",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        StaffHorizontalDivider(alpha = 0.15f)
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(QuizTokens.spacingLarge - 4.dp),
            verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingLarge),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
                StaffFieldLabel(text = "問題形式")
                ExposedDropdownMenuBox(
                    expanded = typeMenuExpanded,
                    onExpandedChange = { typeMenuExpanded = it },
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    OutlinedTextField(
                        value = draft.type.label,
                        onValueChange = {},
                        readOnly = true,
                        singleLine = true,
                        shape = RoundedCornerShape(QuizTokens.cornerSmall),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = typeMenuExpanded) },
                        colors = staffFieldColors(),
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
            }

            Column(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    StaffFieldLabel(text = "問題文", hint = "(Markdown)")
                    StaffTextButton(
                        text = "プレビュー",
                        icon = Icons.Default.Visibility,
                        onClick = { showPromptPreview = !showPromptPreview },
                    )
                }
                OutlinedTextField(
                    value = draft.prompt,
                    onValueChange = { onDraftChange(draft.copy(prompt = it)) },
                    shape = RoundedCornerShape(QuizTokens.cornerSmall),
                    colors = staffFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(144.dp),
                )
                if (showPromptPreview && draft.prompt.isNotBlank()) {
                    QuizMarkdownText(draft.prompt)
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surfaceContainer, RoundedCornerShape(QuizTokens.cornerMedium))
                    .border(1.dp, staffDividerColor(0.1f), RoundedCornerShape(QuizTokens.cornerMedium))
                    .padding(QuizTokens.spacingMedium),
            ) {
                StaffChoiceListEditor(draft = draft, onDraftChange = onDraftChange)
            }

            Column(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom,
                ) {
                    StaffFieldLabel(text = "解説", hint = "(Markdown)")
                    StaffTextButton(
                        text = "プレビュー",
                        icon = Icons.Default.Visibility,
                        onClick = { showExplanationPreview = !showExplanationPreview },
                    )
                }
                OutlinedTextField(
                    value = draft.explanationMarkdown,
                    onValueChange = { onDraftChange(draft.copy(explanationMarkdown = it)) },
                    shape = RoundedCornerShape(QuizTokens.cornerSmall),
                    colors = staffFieldColors(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                )
                if (showExplanationPreview && draft.explanationMarkdown.isNotBlank()) {
                    QuizMarkdownText(draft.explanationMarkdown)
                }
            }
        }
        StaffHorizontalDivider(alpha = 0.15f)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuizTokens.spacingMedium),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            TextButton(onClick = onDismiss) {
                Text("キャンセル", style = MaterialTheme.typography.labelLarge)
            }
            Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
            Button(
                onClick = onSave,
                shape = RoundedCornerShape(percent = 50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                elevation = null,
                modifier = Modifier.height(40.dp),
            ) {
                Text("保存", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
internal fun StaffFieldLabel(text: String, modifier: Modifier = Modifier, hint: String? = null) {
    Row(modifier = modifier, verticalAlignment = Alignment.Bottom) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium,
        )
        if (hint != null) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = hint,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
internal fun staffFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedContainerColor = MaterialTheme.colorScheme.surface,
    unfocusedContainerColor = MaterialTheme.colorScheme.surface,
    focusedBorderColor = MaterialTheme.colorScheme.primary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline,
)
