package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

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
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .height(reorderRowHeight * itemIds.size.coerceAtLeast(1)),
        userScrollEnabled = false,
    ) {
        itemsIndexed(itemIds, key = { _, id -> id }) { index, id ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                QuizReorderRow(
                    label = labelForId(id),
                    modifier = Modifier.weight(1f),
                    reorderEnabled = enabled,
                )
                if (enabled) {
                    Column(modifier = Modifier.padding(end = QuizTokens.spacingSmall)) {
                        IconButton(
                            onClick = { if (index > 0) onMove(index, index - 1) },
                            enabled = index > 0,
                        ) {
                            Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "上へ")
                        }
                        IconButton(
                            onClick = {
                                if (index < itemIds.lastIndex) onMove(index, index + 1)
                            },
                            enabled = index < itemIds.lastIndex,
                        ) {
                            Icon(Icons.Filled.KeyboardArrowDown, contentDescription = "下へ")
                        }
                    }
                }
            }
        }
    }
}
