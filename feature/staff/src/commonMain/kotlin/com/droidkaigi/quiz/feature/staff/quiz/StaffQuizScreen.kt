package com.droidkaigi.quiz.feature.staff.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.ui.components.QuizSurfaceCard
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun StaffQuizScreen(
    viewModel: StaffQuizViewModel = viewModel { StaffQuizViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    StaffQuizContent(
        quizTitle = state.quizSet?.title,
        questions = state.quizSet?.questions.orEmpty(),
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
    )
}

@Composable
fun StaffQuizContent(
    quizTitle: String?,
    questions: List<Question>,
    isLoading: Boolean,
    errorMessage: String?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(QuizTokens.spacingLarge),
    ) {
        Text(
            text = quizTitle ?: "クイズ内容",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
        when {
            isLoading -> {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyLarge,
                )
            }
            questions.isEmpty() -> {
                Text(
                    text = "問題がありません",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium),
                ) {
                    itemsIndexed(questions) { index, question ->
                        StaffQuestionCard(index = index + 1, question = question)
                    }
                }
            }
        }
    }
}

@Composable
private fun StaffQuestionCard(
    index: Int,
    question: Question,
) {
    QuizSurfaceCard(modifier = Modifier.fillMaxWidth()) {
        Column(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
            Text(
                text = "Q$index · ${questionTypeLabel(question)} · ${question.id}",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = question.prompt,
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "正解: ${correctAnswerText(question)}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary,
            )
        }
    }
}
