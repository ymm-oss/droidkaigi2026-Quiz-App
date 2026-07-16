package com.droidkaigi.quiz.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import com.droidkaigi.quiz.core.ui.theme.QuizMotion
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun ChoiceCard(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val animScale = QuizMotion.animateSelectionScale(selected)
    val containerColor by animateColorAsState(
        targetValue = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
    )
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = QuizTokens.spacingSmall)
            .scale(animScale)
            .testTag("choice:$label")
            .clickable(enabled = enabled, onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(QuizTokens.spacingMedium),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}
