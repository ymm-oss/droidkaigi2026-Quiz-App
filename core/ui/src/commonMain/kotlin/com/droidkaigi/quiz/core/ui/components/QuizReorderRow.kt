package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun QuizReorderRow(
    index: Int,
    label: String,
    canMoveUp: Boolean,
    canMoveDown: Boolean,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = QuizTokens.spacingSmall),
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = QuizTokens.spacingSmall / 2),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuizTokens.spacingMedium),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "${index + 1}. $label",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Row(horizontalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
                if (canMoveUp) {
                    FilledTonalButton(
                        onClick = onMoveUp,
                        enabled = enabled,
                        shape = RoundedCornerShape(QuizTokens.cornerSmall),
                    ) {
                        Text("↑")
                    }
                }
                if (canMoveDown) {
                    FilledTonalButton(
                        onClick = onMoveDown,
                        enabled = enabled,
                        shape = RoundedCornerShape(QuizTokens.cornerSmall),
                    ) {
                        Text("↓")
                    }
                }
            }
        }
    }
}
