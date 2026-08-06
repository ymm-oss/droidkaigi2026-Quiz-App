package com.droidkaigi.quiz.feature.staff.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

/** Hairline separators; kept low-contrast so structure never competes with data. */
@Composable
fun staffDividerColor(alpha: Float = 0.2f): Color = MaterialTheme.colorScheme.outline.copy(alpha = alpha)

@Composable
fun StaffVerticalDivider(modifier: Modifier = Modifier, alpha: Float = 0.2f) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .width(1.dp)
            .background(staffDividerColor(alpha)),
    )
}

@Composable
fun StaffHorizontalDivider(modifier: Modifier = Modifier, alpha: Float = 0.2f) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(staffDividerColor(alpha)),
    )
}

/** Filled accent action ("問題を追加", "更新"). */
@Composable
fun StaffFilledButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        shape = RoundedCornerShape(QuizTokens.cornerSmall),
        contentPadding = PaddingValues(horizontal = QuizTokens.spacingLarge, vertical = 0.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
        elevation = null,
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Low-emphasis accent action ("再読込", "Logout"). */
@Composable
fun StaffTextButton(
    text: String,
    icon: ImageVector?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = RoundedCornerShape(QuizTokens.cornerSmall),
        contentPadding = PaddingValues(horizontal = QuizTokens.spacingMedium, vertical = QuizTokens.spacingSmall),
    ) {
        if (icon != null) {
            Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(18.dp), tint = color)
            Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
        }
        Text(text = text, style = MaterialTheme.typography.labelLarge, color = color)
    }
}

/** Outlined action pinned to the sidebar footer ("参加者向けに公開"). */
@Composable
fun StaffOutlinedButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(40.dp),
        enabled = enabled,
        shape = RoundedCornerShape(QuizTokens.cornerSmall),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary),
        contentPadding = PaddingValues(horizontal = QuizTokens.spacingMedium, vertical = 0.dp),
    ) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
        Text(text = text, style = MaterialTheme.typography.labelLarge)
    }
}

/** Neutral metadata chip (question id, etc.). */
@Composable
fun StaffBadge(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .border(1.dp, staffDividerColor(), RoundedCornerShape(4.dp))
            .padding(horizontal = QuizTokens.spacingSmall, vertical = 2.dp),
    )
}

/**
 * Color-coded question-type chip (Stitch: larger pill, distinct container tones).
 * Single → primaryContainer · Multiple → secondary · Reorder → tertiaryContainer.
 */
@Composable
fun StaffQuestionTypeChip(text: String, type: StaffQuestionTypeTone, modifier: Modifier = Modifier) {
    val background = when (type) {
        StaffQuestionTypeTone.SingleChoice -> MaterialTheme.colorScheme.primaryContainer
        // secondaryContainer matches question-card fill; use secondary for contrast on cards.
        StaffQuestionTypeTone.MultipleChoice -> MaterialTheme.colorScheme.secondary
        StaffQuestionTypeTone.Reorder -> MaterialTheme.colorScheme.tertiaryContainer
    }
    val content = when (type) {
        StaffQuestionTypeTone.SingleChoice -> MaterialTheme.colorScheme.onPrimaryContainer
        StaffQuestionTypeTone.MultipleChoice -> MaterialTheme.colorScheme.onSecondary
        StaffQuestionTypeTone.Reorder -> MaterialTheme.colorScheme.onTertiaryContainer
    }
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = content,
        modifier = modifier
            .clip(CircleShape)
            .background(background)
            .border(1.dp, staffDividerColor(), CircleShape)
            .padding(horizontal = 12.dp, vertical = 4.dp),
    )
}

enum class StaffQuestionTypeTone {
    SingleChoice,
    MultipleChoice,
    Reorder,
}

/** Accent pill marking the folder participants currently see. */
@Composable
fun StaffActivePill(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        fontSize = 10.sp,
        color = MaterialTheme.colorScheme.onPrimary,
        modifier = modifier
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.primary)
            .padding(horizontal = QuizTokens.spacingSmall, vertical = 2.dp),
    )
}

/** Content pane wrapper: centered column capped at the readable line length from the design system. */
@Composable
fun StaffContentPane(
    modifier: Modifier = Modifier,
    maxWidth: Dp = QuizTokens.staffContentMaxWidth,
    content: @Composable ColumnScope.() -> Unit,
) {
    Box(modifier = modifier.fillMaxWidth(), contentAlignment = Alignment.TopCenter) {
        Column(
            modifier = Modifier
                .widthIn(max = maxWidth)
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(QuizTokens.spacingLarge),
            content = content,
        )
    }
}

@Composable
fun StaffSectionHeader(
    title: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    actions: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom,
    ) {
        Column {
            Text(text = title, style = MaterialTheme.typography.headlineMedium)
            if (!subtitle.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium),
            content = actions,
        )
    }
}
