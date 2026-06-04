package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun QuizSurfaceCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val containerColor = if (isSystemInDarkTheme()) {
        MaterialTheme.colorScheme.surface
    } else {
        QuizTokens.surfaceElevated
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(QuizTokens.cornerExtraLarge),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        elevation = CardDefaults.cardElevation(defaultElevation = QuizTokens.spacingSmall),
    ) {
        Column(
            modifier = Modifier.padding(QuizTokens.spacingLarge),
            content = content,
        )
    }
}
