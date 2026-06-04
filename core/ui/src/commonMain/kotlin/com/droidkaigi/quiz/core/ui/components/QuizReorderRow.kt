package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun QuizReorderRow(
    label: String,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
    reorderEnabled: Boolean = true,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(if (isDragging) 8.dp else 0.dp, RoundedCornerShape(QuizTokens.cornerMedium))
            .padding(vertical = QuizTokens.spacingSmall),
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = QuizTokens.spacingSmall / 2),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = QuizTokens.spacingMedium,
                    end = QuizTokens.spacingSmall,
                    top = QuizTokens.spacingMedium,
                    bottom = QuizTokens.spacingMedium,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "並び替え",
                modifier = dragHandleModifier
                    .size(48.dp)
                    .padding(QuizTokens.spacingSmall),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = if (reorderEnabled) 1f else 0.38f,
                ),
            )
        }
    }
}
