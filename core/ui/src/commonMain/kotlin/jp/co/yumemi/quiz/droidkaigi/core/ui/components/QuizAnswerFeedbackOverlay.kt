package jp.co.yumemi.quiz.droidkaigi.core.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.Res
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.feedback_correct
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.feedback_correct_answer
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.feedback_explanation
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.feedback_incorrect
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizMotion
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.quizShake
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuizAnswerFeedbackOverlay(
    isCorrect: Boolean,
    correctAnswer: String,
    explanationMarkdown: String,
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

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .testTag("answerFeedbackOverlay")
            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.72f))
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical)),
        contentAlignment = Alignment.Center,
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {},
                ),
        )
        Surface(
            modifier = Modifier
                .padding(QuizTokens.spacingMedium)
                .fillMaxWidth()
                .widthIn(max = 420.dp)
                .heightIn(max = maxHeight - QuizTokens.spacingExtraLarge)
                .scale(revealScale),
            shape = RoundedCornerShape(QuizTokens.cornerLarge),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = QuizTokens.spacingSmall,
        ) {
            Column(
                modifier = Modifier.padding(QuizTokens.spacingLarge),
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium),
            ) {
                Row(
                    modifier = Modifier.quizShake(shakeOffset),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium),
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .scale(1f + pulseAlpha * 0.2f)
                            .background(accent, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Icon(
                            imageVector = if (isCorrect) Icons.Default.Check else Icons.Default.Close,
                            contentDescription = null,
                            modifier = Modifier.size(28.dp),
                            tint = Color.White,
                        )
                    }
                    Text(
                        text = feedbackText,
                        style = MaterialTheme.typography.headlineMedium,
                        color = accent,
                    )
                }

                if (correctAnswer.isNotBlank()) {
                    Column(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
                        Text(
                            text = stringResource(Res.string.feedback_correct_answer),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = correctAnswer,
                            modifier = Modifier.testTag("feedbackCorrectAnswer"),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }

                if (explanationMarkdown.isNotBlank()) {
                    Column(
                        modifier = Modifier
                            .weight(1f, fill = false)
                            .verticalScroll(rememberScrollState())
                            .testTag("feedbackExplanation"),
                        verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
                    ) {
                        Text(
                            text = stringResource(Res.string.feedback_explanation),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        QuizMarkdownText(explanationMarkdown)
                    }
                }

                QuizPrimaryButton(
                    text = continueLabel,
                    onClick = onContinue,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("feedbackContinue"),
                    enabled = continueEnabled,
                    loading = continueLoading,
                )
            }
        }
    }
}
