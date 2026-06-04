package com.droidkaigi.quiz.feature.quiz.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.ui.components.ChoiceCard
import com.droidkaigi.quiz.core.ui.components.QuizFeedbackText
import com.droidkaigi.quiz.core.ui.components.QuizPrimaryButton
import com.droidkaigi.quiz.core.ui.components.QuizProgressHeader
import com.droidkaigi.quiz.core.ui.components.QuizReorderRow
import com.droidkaigi.quiz.core.ui.components.QuizScreenBackground
import com.droidkaigi.quiz.core.ui.components.QuizSurfaceCard
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.core.ui.theme.quizShake

@Composable
fun QuizScreen(
    onFinished: () -> Unit,
    viewModel: QuizViewModel = viewModel { QuizViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                QuizEvent.NavigateToResult -> onFinished()
            }
        }
    }

    val shakeOffset by animateFloatAsState(
        targetValue = if (state.showFeedback && state.lastAnswerCorrect == false) 8f else 0f,
        label = "shake",
    )

    QuizScreenBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = QuizTokens.spacingLarge)
                    .quizShake(shakeOffset)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingLarge),
            ) {
                Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
                QuizProgressHeader(
                    progressLabel = state.progress,
                    progressFraction = state.progressFraction,
                )
                QuizSurfaceCard {
                    Text(
                        text = "問題",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
                    Text(
                        text = state.prompt,
                        style = MaterialTheme.typography.titleLarge,
                    )
                }
                QuizSurfaceCard {
                    Text(
                        text = "回答を選んでください",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    when (val q = state.question) {
                        is SingleChoice -> q.options.forEach { option ->
                            ChoiceCard(
                                label = option.label,
                                selected = state.selectedSingleId == option.id,
                                onClick = { viewModel.onIntent(QuizIntent.SelectSingle(option.id)) },
                                enabled = !state.showFeedback,
                            )
                        }
                        is MultipleChoice -> q.options.forEach { option ->
                            ChoiceCard(
                                label = option.label,
                                selected = option.id in state.selectedMultipleIds,
                                onClick = { viewModel.onIntent(QuizIntent.ToggleMultiple(option.id)) },
                                enabled = !state.showFeedback,
                            )
                        }
                        is Reorder -> {
                            state.reorderIds.forEachIndexed { index, id ->
                                val label = q.items.first { it.id == id }.label
                                QuizReorderRow(
                                    index = index,
                                    label = label,
                                    canMoveUp = index > 0,
                                    canMoveDown = index < state.reorderIds.lastIndex,
                                    onMoveUp = {
                                        viewModel.onIntent(QuizIntent.MoveReorder(index, index - 1))
                                    },
                                    onMoveDown = {
                                        viewModel.onIntent(QuizIntent.MoveReorder(index, index + 1))
                                    },
                                    enabled = !state.showFeedback,
                                )
                            }
                        }
                        null -> Text("問題がありません")
                    }
                }
                AnimatedVisibility(visible = state.showFeedback && state.lastAnswerCorrect != null) {
                    state.lastAnswerCorrect?.let { correct ->
                        QuizFeedbackText(isCorrect = correct)
                    }
                }
                QuizPrimaryButton(
                    text = "回答する",
                    onClick = { viewModel.onIntent(QuizIntent.SubmitAnswer) },
                    enabled = state.canSubmit && !state.showFeedback,
                )
                Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
            }
        }
    }
}
