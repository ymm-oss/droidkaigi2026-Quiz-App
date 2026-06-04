package com.droidkaigi.quiz.feature.quiz.quiz

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.ui.components.ChoiceCard
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(QuizTokens.spacingMedium)
            .quizShake(shakeOffset)
            .verticalScroll(rememberScrollState()),
    ) {
        Text(text = state.progress, style = MaterialTheme.typography.labelLarge)
        Text(
            text = state.prompt,
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(vertical = QuizTokens.spacingMedium),
        )

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
                FilterChip(
                    selected = option.id in state.selectedMultipleIds,
                    onClick = { viewModel.onIntent(QuizIntent.ToggleMultiple(option.id)) },
                    label = { Text(option.label) },
                    modifier = Modifier.padding(QuizTokens.spacingSmall),
                    enabled = !state.showFeedback,
                )
            }
            is Reorder -> {
                state.reorderIds.forEachIndexed { index, id ->
                    val label = q.items.first { it.id == id }.label
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = QuizTokens.spacingSmall),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text("${index + 1}. $label", modifier = Modifier.weight(1f))
                        Row {
                            if (index > 0) {
                                Button(
                                    onClick = { viewModel.onIntent(QuizIntent.MoveReorder(index, index - 1)) },
                                    enabled = !state.showFeedback,
                                ) { Text("↑") }
                            }
                            if (index < state.reorderIds.lastIndex) {
                                Button(
                                    onClick = { viewModel.onIntent(QuizIntent.MoveReorder(index, index + 1)) },
                                    enabled = !state.showFeedback,
                                ) { Text("↓") }
                            }
                        }
                    }
                }
            }
            null -> Text("問題がありません")
        }

        AnimatedVisibility(visible = state.showFeedback) {
            val msg = when (state.lastAnswerCorrect) {
                true -> "正解！"
                false -> "不正解"
                null -> ""
            }
            Text(
                text = msg,
                color = if (state.lastAnswerCorrect == true) QuizTokens.correct else QuizTokens.incorrect,
                modifier = Modifier.padding(top = QuizTokens.spacingMedium),
            )
        }

        Button(
            onClick = { viewModel.onIntent(QuizIntent.SubmitAnswer) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = QuizTokens.spacingLarge),
            enabled = state.canSubmit && !state.showFeedback,
        ) {
            Text("回答する")
        }
    }
}
