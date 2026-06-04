package com.droidkaigi.quiz.feature.staff.quiz

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.feature.staff.StaffConfirmDialog
import com.droidkaigi.quiz.feature.staff.truncateForDialog
import com.droidkaigi.quiz.core.ui.components.QuizMarkdownText
import com.droidkaigi.quiz.core.ui.components.QuizPrimaryButton
import com.droidkaigi.quiz.core.ui.components.QuizSurfaceCard
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun StaffQuizScreen(
    folderId: String,
    viewModel: StaffQuizViewModel = viewModel(key = folderId) { StaffQuizViewModel(folderId) },
) {
    val state by viewModel.uiState.collectAsState()
    var questionToDelete by remember { mutableStateOf<Question?>(null) }
    val draft = state.editorDraft
    if (draft != null) {
        StaffQuestionEditorDialog(
            draft = draft,
            isNew = state.isNewQuestion,
            onDraftChange = { viewModel.onIntent(StaffQuizIntent.UpdateEditorDraft(it)) },
            onDismiss = { viewModel.onIntent(StaffQuizIntent.DismissEditor) },
            onSave = { viewModel.onIntent(StaffQuizIntent.SaveEditor) },
        )
    }
    StaffQuizContent(
        quizTitle = state.quizSet?.title,
        questions = state.quizSet?.questions.orEmpty(),
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRefresh = { viewModel.onIntent(StaffQuizIntent.Refresh) },
        onAddQuestion = { viewModel.onIntent(StaffQuizIntent.AddQuestion) },
        onEditQuestion = { viewModel.onIntent(StaffQuizIntent.EditQuestion(it)) },
        onRequestDeleteQuestion = { questionToDelete = it },
        onReorderQuestions = { from, to ->
            viewModel.onIntent(StaffQuizIntent.ReorderQuestions(from, to))
        },
    )
    val deleteTarget = questionToDelete
    if (deleteTarget != null) {
        StaffConfirmDialog(
            title = "問題を削除",
            message = "「${truncateForDialog(deleteTarget.prompt)}」を削除しますか？\nこの操作は取り消せません。",
            confirmLabel = "削除",
            onConfirm = {
                viewModel.onIntent(StaffQuizIntent.DeleteQuestion(deleteTarget.id))
                questionToDelete = null
            },
            onDismiss = { questionToDelete = null },
        )
    }
}

@Composable
fun StaffQuizContent(
    quizTitle: String?,
    questions: List<Question>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    onAddQuestion: () -> Unit,
    onEditQuestion: (Question) -> Unit,
    onRequestDeleteQuestion: (Question) -> Unit,
    onReorderQuestions: (fromIndex: Int, toIndex: Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(QuizTokens.spacingLarge),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = quizTitle ?: "クイズ内容",
                style = MaterialTheme.typography.headlineMedium,
            )
            TextButton(onClick = onRefresh) { Text("再読込") }
        }
        Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
        QuizPrimaryButton(text = "問題を追加", onClick = onAddQuestion)
        Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
        if (questions.isNotEmpty()) {
            Text(
                text = "ドラッグで問題の出題順を並び替え",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
        }
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
                StaffQuestionReorderList(
                    questions = questions,
                    onMove = onReorderQuestions,
                    onEdit = onEditQuestion,
                    onRequestDelete = onRequestDeleteQuestion,
                    modifier = Modifier.weight(1f),
                )
            }
        }
    }
}

@Composable
internal fun StaffQuestionCard(
    index: Int,
    question: Question,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    isDragging: Boolean = false,
    dragHandleModifier: Modifier = Modifier,
) {
    QuizSurfaceCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = QuizTokens.spacingSmall)
            .shadow(if (isDragging) 8.dp else 0.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
        ) {
            Icon(
                imageVector = Icons.Filled.Menu,
                contentDescription = "並び替え",
                modifier = dragHandleModifier
                    .size(48.dp)
                    .padding(QuizTokens.spacingSmall),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = "Q$index · ${questionTypeLabel(question)} · ${question.id}",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row {
                        TextButton(onClick = onEdit) { Text("編集") }
                        TextButton(onClick = onDelete) { Text("削除") }
                    }
                }
                Text(
                    text = question.prompt,
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(
                    text = "正解: ${correctAnswerText(question)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                if (question.explanationMarkdown.isNotBlank()) {
                    Text(
                        text = "解説",
                        style = MaterialTheme.typography.labelMedium,
                    )
                    QuizMarkdownText(question.explanationMarkdown)
                }
            }
        }
    }
}
