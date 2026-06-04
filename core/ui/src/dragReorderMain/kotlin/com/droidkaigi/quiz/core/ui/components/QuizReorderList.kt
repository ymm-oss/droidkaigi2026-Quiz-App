package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import org.burnoutcrew.reorderable.ReorderableItem
import org.burnoutcrew.reorderable.detectReorder
import org.burnoutcrew.reorderable.rememberReorderableLazyListState
import org.burnoutcrew.reorderable.reorderable

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
    val reorderState = rememberReorderableLazyListState(
        onMove = { from, to -> onMove(from.index, to.index) },
    )
    val listModifier = modifier
        .fillMaxWidth()
        .then(if (enabled) Modifier.reorderable(reorderState) else Modifier)

    LazyColumn(
        state = reorderState.listState,
        modifier = listModifier.height(reorderRowHeight * itemIds.size.coerceAtLeast(1)),
        userScrollEnabled = false,
    ) {
        items(itemIds, key = { it }) { id ->
            ReorderableItem(
                state = reorderState,
                key = id,
                defaultDraggingModifier = Modifier.animateItem(),
            ) { isDragging ->
                QuizReorderRow(
                    label = labelForId(id),
                    isDragging = isDragging,
                    dragHandleModifier = if (enabled) {
                        Modifier.detectReorder(reorderState)
                    } else {
                        Modifier
                    },
                    reorderEnabled = enabled,
                )
            }
        }
    }
}
