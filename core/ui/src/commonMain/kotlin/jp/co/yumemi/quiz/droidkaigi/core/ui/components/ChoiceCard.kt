package jp.co.yumemi.quiz.droidkaigi.core.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.hideFromAccessibility
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizMotion
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens

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
            .semantics {
                this.selected = selected
                role = Role.Button
            }
            .testTag("choice:$label"),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
        enabled = enabled,
        onClick = onClick,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(QuizTokens.spacingMedium),
            style = MaterialTheme.typography.bodyLarge,
        )
    }
}

@Composable
fun MultipleChoiceCard(
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
            .scale(animScale),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 48.dp)
                .toggleable(
                    value = selected,
                    enabled = enabled,
                    role = Role.Checkbox,
                    onValueChange = { onClick() },
                )
                .testTag("choice:$label")
                .padding(
                    horizontal = QuizTokens.spacingMedium,
                    vertical = QuizTokens.spacingSmall,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = null,
                enabled = enabled,
                modifier = Modifier
                    .size(QuizTokens.spacingLarge)
                    .semantics { hideFromAccessibility() },
            )
            Text(
                text = label,
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyLarge,
            )
        }
    }
}
