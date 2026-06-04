package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorderAfterLongPress
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

@Composable
fun QuizReorderList(
    itemIds: List<String>,
    labelForId: (String) -> String,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> onMove(from.index, to.index) },
    )
    val listModifier = modifier
        .fillMaxWidth()
        .then(
            if (enabled) {
                Modifier
                    .reorderable(reorderState)
                    .detectReorderAfterLongPress(reorderState)
            } else {
                Modifier
            },
        )

    LazyColumn(
        state = reorderState.listState,
        modifier = listModifier,
        userScrollEnabled = false,
    ) {
        itemsIndexed(itemIds, key = { _, id -> id }) { index, id ->
            ReorderableItem(reorderState, key = id) { isDragging ->
                QuizReorderRow(
                    index = index,
                    label = labelForId(id),
                    isDragging = isDragging,
                )
            }
        }
    }
}
