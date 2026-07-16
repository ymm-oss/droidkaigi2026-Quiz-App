package com.droidkaigi.quiz.feature.staff.quiz

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.domain.model.Question
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

@Composable
fun StaffQuestionReorderList(
    questions: List<Question>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onEdit: (Question) -> Unit,
    onRequestDelete: (Question) -> Unit,
    modifier: Modifier = Modifier,
) {
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
    }
    LazyColumn(
        state = lazyListState,
        modifier = modifier.fillMaxWidth(),
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
                    dragHandleModifier = Modifier.longPressDraggableHandle(),
                    onEdit = { onEdit(question) },
                    onDelete = { onRequestDelete(question) },
                )
            }
        }
    }
}
