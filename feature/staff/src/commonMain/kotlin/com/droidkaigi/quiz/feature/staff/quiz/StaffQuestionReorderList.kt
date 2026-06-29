package com.droidkaigi.quiz.feature.staff.quiz

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.domain.model.Question
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun StaffQuestionReorderList(
    questions: List<Question>,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    onEdit: (Question) -> Unit,
    onRequestDelete: (Question) -> Unit,
    modifier: Modifier = Modifier,
) {
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> onMove(from.index, to.index) },
    )
    LazyColumn(
        state = reorderState.listState,
        modifier = modifier
            .fillMaxWidth()
            .reorderable(reorderState),
    ) {
        itemsIndexed(questions, key = { _, question -> question.id }) { index, question ->
            ReorderableItem(
                state = reorderState,
                key = question.id,
            ) { isDragging ->
                StaffQuestionCard(
                    index = index + 1,
                    question = question,
                    isDragging = isDragging,
                    dragHandleModifier = Modifier.detectReorder(reorderState),
                    onEdit = { onEdit(question) },
                    onDelete = { onRequestDelete(question) },
                )
            }
        }
    }
}
