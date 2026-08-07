package com.droidkaigi.quiz.feature.quiz.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.ui.components.ChoiceCard
import com.droidkaigi.quiz.core.ui.components.QuizAnswerFeedbackOverlay
import com.droidkaigi.quiz.core.ui.components.QuizMarkdownText
import com.droidkaigi.quiz.core.ui.components.QuizPrimaryButton
import com.droidkaigi.quiz.core.ui.components.QuizProgressHeader
import com.droidkaigi.quiz.core.ui.components.QuizReorderList
import com.droidkaigi.quiz.core.ui.components.QuizScreenBackground
import com.droidkaigi.quiz.core.ui.components.QuizSurfaceCard
import com.droidkaigi.quiz.core.ui.generated.resources.Res
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_exit_cancel
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_exit_confirm
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_exit_message
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_exit_title
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_feedback_finish
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_feedback_next
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_instruction_choice
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_instruction_reorder
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_no_question
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_section_question
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_submit
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_submit_score_failed_message
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_submit_score_failed_title
import com.droidkaigi.quiz.core.ui.generated.resources.quiz_submit_score_retry
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.core.ui.theme.quizSafeHorizontalPadding
import com.droidkaigi.quiz.core.ui.theme.quizSafeVerticalPadding
import kotlinx.coroutines.flow.Flow
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuizScreen(
    onFinished: () -> Unit,
    onAbandoned: () -> Unit,
    leaveRequest: Flow<Unit>,
    onExitEnabledChange: (Boolean) -> Unit = {},
    submitScore: Boolean = true,
) {
    val sessionKey = AppDependencies.shared.sessionHolder.currentSession?.startedAtEpochMillis
    val viewModel: QuizViewModel = viewModel(
        key = "${sessionKey?.toString() ?: "no-session"}-submit=$submitScore",
    ) {
        QuizViewModel(submitScore = submitScore)
    }
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(sessionKey) {
        if (sessionKey == null) {
            // Saved navigation state can restore this screen after process death,
            // but the in-memory session is gone — return to Home instead of a blank quiz.
            onAbandoned()
        } else {
            viewModel.syncFromSession()
        }
    }

    LaunchedEffect(state.isFinishing) {
        onExitEnabledChange(!state.isFinishing)
    }

    DisposableEffect(Unit) {
        onDispose { onExitEnabledChange(true) }
    }

    LaunchedEffect(leaveRequest) {
        leaveRequest.collect {
            viewModel.onIntent(QuizIntent.RequestExit)
        }
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                QuizEvent.NavigateToResult -> onFinished()
                QuizEvent.NavigateHome -> onAbandoned()
            }
        }
    }

    QuizContent(
        state = state,
        onSelectSingle = { viewModel.onIntent(QuizIntent.SelectSingle(it)) },
        onToggleMultiple = { viewModel.onIntent(QuizIntent.ToggleMultiple(it)) },
        onMoveReorder = { from, to -> viewModel.onIntent(QuizIntent.MoveReorder(from, to)) },
        onSubmitAnswer = { viewModel.onIntent(QuizIntent.SubmitAnswer) },
        onContinueAfterFeedback = { viewModel.onIntent(QuizIntent.ContinueAfterFeedback) },
        onRetrySubmitScore = { viewModel.onIntent(QuizIntent.RetrySubmitScore) },
    )

    if (state.showExitConfirm) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(QuizIntent.DismissExit) },
            title = { Text(stringResource(Res.string.quiz_exit_title)) },
            text = {
                Text(
                    text = stringResource(Res.string.quiz_exit_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(QuizIntent.ConfirmExit) }) {
                    Text(stringResource(Res.string.quiz_exit_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(QuizIntent.DismissExit) }) {
                    Text(stringResource(Res.string.quiz_exit_cancel))
                }
            },
        )
    }
}

@Composable
fun QuizContent(
    state: QuizUiState,
    onSelectSingle: (String) -> Unit,
    onToggleMultiple: (String) -> Unit,
    onMoveReorder: (Int, Int) -> Unit,
    onSubmitAnswer: () -> Unit,
    onContinueAfterFeedback: () -> Unit,
    onRetrySubmitScore: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    QuizScreenBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .quizSafeHorizontalPadding(),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .quizSafeVerticalPadding()
                    .padding(horizontal = QuizTokens.spacingLarge),
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingLarge),
            ) {
                QuizProgressHeader(
                    progressLabel = state.progress,
                    progressFraction = state.progressFraction,
                )
                QuizSurfaceCard {
                    Text(
                        text = stringResource(Res.string.quiz_section_question),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
                    QuizMarkdownText(state.prompt)
                }
                QuizSurfaceCard {
                    Text(
                        text = when (state.question) {
                            is Reorder -> stringResource(Res.string.quiz_instruction_reorder)

                            is MultipleChoice,
                            is SingleChoice,
                            null,
                            -> stringResource(Res.string.quiz_instruction_choice)
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
                QuizPrimaryButton(
                    text = stringResource(Res.string.quiz_submit),
                    onClick = onSubmitAnswer,
                    enabled = state.canSubmit && !state.showFeedback,
                    modifier = Modifier.testTag("quizSubmit"),
                )
            }

            val answerCorrect = state.lastAnswerCorrect
            val showFeedbackOverlay = state.showFeedback && answerCorrect != null
            var cachedFeedbackCorrect by remember { mutableStateOf(false) }
            LaunchedEffect(answerCorrect) {
                if (answerCorrect != null) {
                    cachedFeedbackCorrect = answerCorrect
                }
            }
            AnimatedVisibility(
                visible = showFeedbackOverlay,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                QuizAnswerFeedbackOverlay(
                    isCorrect = answerCorrect ?: cachedFeedbackCorrect,
                    continueLabel = stringResource(
                        if (state.isFinishing) {
                            Res.string.quiz_feedback_finish
                        } else {
                            Res.string.quiz_feedback_next
                        },
                    ),
                    onContinue = onContinueAfterFeedback,
                    continueEnabled = state.submitPhase != SubmitPhase.Submitting &&
                        state.submitPhase != SubmitPhase.Failed,
                    continueLoading = state.submitPhase == SubmitPhase.Submitting,
                )
            }
        }
    }

    if (state.submitPhase == SubmitPhase.Failed) {
        AlertDialog(
            onDismissRequest = {},
            title = { Text(stringResource(Res.string.quiz_submit_score_failed_title)) },
            text = {
                Text(
                    text = stringResource(Res.string.quiz_submit_score_failed_message),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
            confirmButton = {
                TextButton(onClick = onRetrySubmitScore) {
                    Text(stringResource(Res.string.quiz_submit_score_retry))
                }
            },
        )
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

        null -> Text(stringResource(Res.string.quiz_no_question))
    }
}
