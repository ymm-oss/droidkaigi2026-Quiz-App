package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun QuizRankingRow(
    rank: Int,
    nickname: String,
    score: Int,
    highlighted: Boolean,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (highlighted) {
        QuizTokens.highlight
    } else {
        MaterialTheme.colorScheme.surface
    }
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = QuizTokens.spacingSmall),
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (highlighted) QuizTokens.spacingSmall else QuizTokens.spacingSmall / 2,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(QuizTokens.spacingMedium),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "$rank",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(end = QuizTokens.spacingMedium),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = nickname,
                    style = MaterialTheme.typography.bodyLarge,
                )
                Text(
                    text = "スコア $score",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
