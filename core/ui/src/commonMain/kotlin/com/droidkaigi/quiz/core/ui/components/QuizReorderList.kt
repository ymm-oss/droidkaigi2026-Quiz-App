package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import sh.calvin.reorderable.ReorderableColumn

/**
 * Nested under the quiz screen's vertical scroll. Uses [ReorderableColumn] (not a fixed-height
 * LazyColumn) so multi-line labels and larger font scales are not clipped.
 */
@Composable
fun QuizReorderList(
    itemIds: List<String>,
    labelForId: (String) -> String,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    ReorderableColumn(
        list = itemIds,
        onSettle = { fromIndex, toIndex ->
            if (enabled && fromIndex != toIndex) {
                onMove(fromIndex, toIndex)
            }
        },
        modifier = modifier.fillMaxWidth(),
    ) { _, id, isDragging ->
        key(id) {
            ReorderableItem {
                QuizReorderRow(
                    label = labelForId(id),
                    isDragging = isDragging,
                    dragHandleModifier = if (enabled) {
                        Modifier.draggableHandle()
                    } else {
                        Modifier
                    },
                    reorderEnabled = enabled,
                )
            }
        }
    }
}
