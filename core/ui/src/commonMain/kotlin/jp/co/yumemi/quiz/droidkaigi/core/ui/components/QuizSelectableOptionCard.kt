package jp.co.yumemi.quiz.droidkaigi.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.isQuizDarkTheme

@Composable
fun QuizSelectableOptionCard(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    subtitle: String? = null,
) {
    val containerColor = when {
        selected -> MaterialTheme.colorScheme.primaryContainer
        isQuizDarkTheme() -> MaterialTheme.colorScheme.surface
        else -> QuizTokens.surfaceElevated
    }
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(QuizTokens.cornerLarge),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        border = if (selected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
        },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(QuizTokens.spacingMedium)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!subtitle.isNullOrBlank()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = QuizTokens.spacingSmall),
                )
            }
        }
    }
}
