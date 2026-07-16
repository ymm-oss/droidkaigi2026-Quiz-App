package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState

private val reorderRowHeight =
    QuizTokens.spacingSmall * 4 +
        QuizTokens.spacingMedium * 2 +
        48.dp

@Composable
fun QuizReorderList(
    itemIds: List<String>,
    labelForId: (String) -> String,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val lazyListState = rememberLazyListState()
    val reorderableState = rememberReorderableLazyListState(lazyListState) { from, to ->
        onMove(from.index, to.index)
    }

    LazyColumn(
        state = lazyListState,
        modifier = modifier
            .fillMaxWidth()
            .height(reorderRowHeight * itemIds.size.coerceAtLeast(1)),
        userScrollEnabled = false,
    ) {
        items(itemIds, key = { it }) { id ->
            ReorderableItem(
                state = reorderableState,
                key = id,
            ) { isDragging ->
                QuizReorderRow(
                    label = labelForId(id),
                    isDragging = isDragging,
                    dragHandleModifier = if (enabled) {
                        Modifier.longPressDraggableHandle()
                    } else {
                        Modifier
                    },
                    reorderEnabled = enabled,
                )
            }
        }
    }
}
