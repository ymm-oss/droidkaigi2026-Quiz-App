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
import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.ui.components.ChoiceCard
import com.droidkaigi.quiz.core.ui.components.QuizFeedbackText
import com.droidkaigi.quiz.core.ui.components.QuizMarkdownText
import com.droidkaigi.quiz.core.ui.components.QuizPrimaryButton
import com.droidkaigi.quiz.core.ui.components.QuizProgressHeader
import com.droidkaigi.quiz.core.ui.components.QuizReorderList
import com.droidkaigi.quiz.core.ui.components.QuizScreenBackground
import com.droidkaigi.quiz.core.ui.components.QuizSurfaceCard
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.core.ui.theme.quizShake

@Composable
fun QuizScreen(
    onFinished: () -> Unit,
) {
    val sessionKey = AppDependencies.shared.sessionHolder.currentSession?.startedAtEpochMillis
    val viewModel: QuizViewModel = viewModel(key = sessionKey?.toString() ?: "no-session") {
        QuizViewModel()
    }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionKey) {
        viewModel.syncFromSession()
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                QuizEvent.NavigateToResult -> onFinished()
            }
        }
    }

    QuizContent(
        state = state,
        onSelectSingle = { viewModel.onIntent(QuizIntent.SelectSingle(it)) },
        onToggleMultiple = { viewModel.onIntent(QuizIntent.ToggleMultiple(it)) },
        onMoveReorder = { from, to -> viewModel.onIntent(QuizIntent.MoveReorder(from, to)) },
        onSubmitAnswer = { viewModel.onIntent(QuizIntent.SubmitAnswer) },
    )
}

@Composable
fun QuizContent(
    state: QuizUiState,
    onSelectSingle: (String) -> Unit,
    onToggleMultiple: (String) -> Unit,
    onMoveReorder: (Int, Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shakeOffset by animateFloatAsState(
        targetValue = if (state.showFeedback && state.lastAnswerCorrect == false) 8f else 0f,
        label = "shake",
    )

    QuizScreenBackground(modifier = modifier) {
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
                    QuizMarkdownText(state.prompt)
                }
                QuizSurfaceCard {
                    Text(
                        text = if (state.question is Reorder) {
                            "上から順に並び替えてください"
                        } else {
                            "回答を選んでください"
                        },
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    QuestionAnswerArea(
                        question = state.question,
                        selectedSingleId = state.selectedSingleId,
                        selectedMultipleIds = state.selectedMultipleIds,
                        reorderIds = state.reorderIds,
                        showFeedback = state.showFeedback,
                        onSelectSingle = onSelectSingle,
                        onToggleMultiple = onToggleMultiple,
                        onMoveReorder = onMoveReorder,
                    )
                }
                AnimatedVisibility(visible = state.showFeedback && state.lastAnswerCorrect != null) {
                    state.lastAnswerCorrect?.let { correct ->
                        QuizFeedbackText(isCorrect = correct)
                    }
                }
                QuizPrimaryButton(
                    text = "回答する",
                    onClick = onSubmitAnswer,
                    enabled = state.canSubmit && !state.showFeedback,
                )
                Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
            }
        }
    }
}

@Composable
private fun QuestionAnswerArea(
    question: Question?,
    selectedSingleId: String?,
    selectedMultipleIds: Set<String>,
    reorderIds: List<String>,
    showFeedback: Boolean,
    onSelectSingle: (String) -> Unit,
    onToggleMultiple: (String) -> Unit,
    onMoveReorder: (Int, Int) -> Unit,
) {
    when (val q = question) {
        is SingleChoice -> q.options.forEach { option ->
            ChoiceCard(
                label = option.label,
                selected = selectedSingleId == option.id,
                onClick = { onSelectSingle(option.id) },
                enabled = !showFeedback,
            )
        }
        is MultipleChoice -> q.options.forEach { option ->
            ChoiceCard(
                label = option.label,
                selected = option.id in selectedMultipleIds,
                onClick = { onToggleMultiple(option.id) },
                enabled = !showFeedback,
            )
        }
        is Reorder -> {
            QuizReorderList(
                itemIds = reorderIds,
                labelForId = { id -> q.items.first { it.id == id }.label },
                onMove = onMoveReorder,
                enabled = !showFeedback,
            )
        }
        null -> Text("問題がありません")
    }
}
