package com.droidkaigi.quiz.feature.staff.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.ui.components.QuizTextField
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun StaffChoiceListEditor(
    draft: StaffQuestionDraft,
    onDraftChange: (StaffQuestionDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
        Text(
            text = when (draft.type) {
                StaffQuestionType.Reorder -> "項目（上から正解の順）"
                StaffQuestionType.MultipleChoice -> "選択肢（正解にチェック）"
                StaffQuestionType.SingleChoice -> "選択肢（正解を1つ選択）"
            },
            style = MaterialTheme.typography.labelLarge,
        )
        if (draft.type == StaffQuestionType.Reorder) {
            Text(
                text = "↑↓ で並び順を変更します。表示順が正解順になります。",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        draft.items.forEachIndexed { index, item ->
            StaffChoiceRow(
                index = index,
                item = item,
                type = draft.type,
                isCorrectSingle = draft.correctSingleId == item.id,
                isCorrectMultiple = item.id in draft.correctMultipleIds,
                canMoveUp = index > 0,
                canMoveDown = index < draft.items.lastIndex,
                canDelete = draft.items.size > 2,
                onLabelChange = { onDraftChange(draft.updateItemLabel(item.id, it)) },
                onSelectSingle = { onDraftChange(draft.copy(correctSingleId = item.id)) },
                onToggleMultiple = { checked ->
                    val updated = if (checked) {
                        draft.correctMultipleIds + item.id
                    } else {
                        draft.correctMultipleIds - item.id
                    }
                    onDraftChange(draft.copy(correctMultipleIds = updated))
                },
                onMoveUp = { onDraftChange(draft.moveItemUp(item.id)) },
                onMoveDown = { onDraftChange(draft.moveItemDown(item.id)) },
                onDelete = { onDraftChange(draft.removeItem(item.id)) },
            )
        }
        TextButton(
            onClick = { onDraftChange(draft.addItem()) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("追加", modifier = Modifier.padding(start = QuizTokens.spacingSmall))
        }
    }
}

@Composable
private fun StaffChoiceRow(
    index: Int,
    item: StaffListItem,
    type: StaffQuestionType,
    isCorrectSingle: Boolean,
    isCorrectMultiple: Boolean,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    canDelete: Boolean,
    onLabelChange: (String) -> Unit,
    onSelectSingle: () -> Unit,
    onToggleMultiple: (Boolean) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
    ) {
        when (type) {
            StaffQuestionType.SingleChoice -> RadioButton(
                selected = isCorrectSingle,
                onClick = onSelectSingle,
            )

            StaffQuestionType.MultipleChoice -> Checkbox(
                checked = isCorrectMultiple,
                onCheckedChange = onToggleMultiple,
            )

            StaffQuestionType.Reorder -> Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.labelLarge,
                modifier = Modifier.padding(horizontal = QuizTokens.spacingSmall),
            )
        }
        QuizTextField(
            value = item.label,
            onValueChange = onLabelChange,
            label = "選択肢 ${index + 1}",
            modifier = Modifier.weight(1f),
        )
        if (type == StaffQuestionType.Reorder) {
            IconButton(onClick = onMoveUp, enabled = canMoveUp) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "上へ")
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "下へ")
            }
        }
        IconButton(onClick = onDelete, enabled = canDelete) {
            Icon(Icons.Default.Delete, contentDescription = "削除")
        }
    }
}
