package com.droidkaigi.quiz.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.droidkaigi.quiz.core.ui.generated.resources.Res
import com.droidkaigi.quiz.core.ui.generated.resources.feedback_correct
import com.droidkaigi.quiz.core.ui.generated.resources.feedback_incorrect
import com.droidkaigi.quiz.core.ui.theme.QuizMotion
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.core.ui.theme.quizShake
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuizAnswerFeedbackOverlay(
    isCorrect: Boolean,
    continueLabel: String,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier,
    continueEnabled: Boolean = true,
    continueLoading: Boolean = false,
) {
    val revealScale = QuizMotion.animateFeedbackReveal(visible = true)
    val accent = if (isCorrect) QuizTokens.correct else QuizTokens.incorrect
    val feedbackText = if (isCorrect) {
        stringResource(Res.string.feedback_correct)
    } else {
        stringResource(Res.string.feedback_incorrect)
    }
    val pulseAlpha by animateFloatAsState(
        targetValue = if (isCorrect) 0.35f else 0f,
        label = "feedbackPulse",
    )
    val shakeOffset by animateFloatAsState(
        targetValue = if (!isCorrect) 8f else 0f,
        label = "feedbackShake",
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .testTag("answerFeedbackOverlay")
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = {},
            )
            .semantics { contentDescription = feedbackText },
        contentAlignment = Alignment.Center,
    ) {
        Column(
            modifier = Modifier
                .widthIn(max = 360.dp)
                .padding(horizontal = QuizTokens.spacingLarge)
                .scale(revealScale),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingLarge),
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.quizShake(shakeOffset),
            ) {
                if (isCorrect) {
                    Box(
                        modifier = Modifier
                            .size(156.dp)
                            .scale(1f + pulseAlpha * 0.2f)
                            .background(accent.copy(alpha = pulseAlpha), CircleShape),
                    )
                }
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .background(accent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                        contentDescription = null,
                        modifier = Modifier.size(72.dp),
                        tint = Color.White,
                    )
                }
            }
            Text(
                text = feedbackText,
                style = MaterialTheme.typography.displaySmall,
                color = Color.White,
            )
            QuizPrimaryButton(
                text = continueLabel,
                onClick = onContinue,
                modifier = Modifier.testTag("feedbackContinue"),
                enabled = continueEnabled,
                loading = continueLoading,
            )
        }
    }
}
