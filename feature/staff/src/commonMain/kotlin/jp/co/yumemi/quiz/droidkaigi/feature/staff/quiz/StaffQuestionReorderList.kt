package jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Question
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun StaffQuestionReorderList(
    questions: List<Question>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onEdit: (Question) -> Unit,
    onRequestDelete: (Question) -> Unit,
    modifier: Modifier = Modifier,
    reorderEnabled: Boolean = true,
) {
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        if (reorderEnabled) {
            onMove(from.index, to.index)
        }
    }
    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
    ) {
        itemsIndexed(questions, key = { _, question -> question.id }) { index, question ->
            ReorderableItem(
                state = reorderableState,
                key = question.id,
            ) { isDragging ->
                StaffQuestionCard(
                    index = index + 1,
                    question = question,
                    isDragging = isDragging,
                    dragHandleModifier = if (reorderEnabled) Modifier.draggableHandle() else Modifier,
                    actionsEnabled = reorderEnabled,
                    onEdit = { onEdit(question) },
                    onDelete = { onRequestDelete(question) },
                )
            }
        }
    }
}
