package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun QuizReorderRow(
    index: Int,
    label: String,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
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
        Text(
            text = "${index + 1}. $label",
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuizTokens.spacingMedium),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
