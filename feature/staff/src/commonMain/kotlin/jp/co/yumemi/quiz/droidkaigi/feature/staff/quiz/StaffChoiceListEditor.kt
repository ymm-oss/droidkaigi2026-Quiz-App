package jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffTextButton

@Composable
fun StaffChoiceListEditor(
    draft: StaffQuestionDraft,
    onDraftChange: (StaffQuestionDraft) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier, verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
        StaffFieldLabel(
            text = when (draft.type) {
                StaffQuestionType.Reorder -> "項目"
                StaffQuestionType.MultipleChoice -> "選択肢"
                StaffQuestionType.SingleChoice -> "選択肢"
            },
            hint = when (draft.type) {
                StaffQuestionType.Reorder -> "上から正解の順"
                StaffQuestionType.MultipleChoice -> "正解にチェック"
                StaffQuestionType.SingleChoice -> "正解を1つ選択"
            },
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
        StaffTextButton(text = "追加", icon = Icons.Default.Add, onClick = { onDraftChange(draft.addItem()) })
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
                modifier = Modifier.size(QuizTokens.spacingLarge),
            )

            StaffQuestionType.MultipleChoice -> Checkbox(
                checked = isCorrectMultiple,
                onCheckedChange = onToggleMultiple,
                modifier = Modifier.size(QuizTokens.spacingLarge),
            )

            StaffQuestionType.Reorder -> Text(
                text = "${index + 1}.",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        OutlinedTextField(
            value = item.label,
            onValueChange = onLabelChange,
            singleLine = true,
            placeholder = {
                Text(
                    text = "選択肢 ${index + 1}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            },
            textStyle = MaterialTheme.typography.bodyMedium,
            shape = RoundedCornerShape(QuizTokens.cornerSmall),
            colors = staffFieldColors(),
            modifier = Modifier.weight(1f),
        )
        if (type == StaffQuestionType.Reorder) {
            IconButton(onClick = onMoveUp, enabled = canMoveUp, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowUpward, contentDescription = "上へ", modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = onMoveDown, enabled = canMoveDown, modifier = Modifier.size(32.dp)) {
                Icon(Icons.Default.ArrowDownward, contentDescription = "下へ", modifier = Modifier.size(18.dp))
            }
        }
        IconButton(onClick = onDelete, enabled = canDelete, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "削除",
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(18.dp),
            )
        }
    }
}
