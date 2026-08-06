package com.droidkaigi.quiz.feature.staff.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.FormatListNumbered
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.ui.components.QuizMarkdownText
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.feature.staff.StaffConfirmDialog
import com.droidkaigi.quiz.feature.staff.components.StaffContentPane
import com.droidkaigi.quiz.feature.staff.components.StaffFilledButton
import com.droidkaigi.quiz.feature.staff.components.StaffQuestionTypeChip
import com.droidkaigi.quiz.feature.staff.components.StaffQuestionTypeTone
import com.droidkaigi.quiz.feature.staff.components.StaffSectionHeader
import com.droidkaigi.quiz.feature.staff.components.StaffTextButton
import com.droidkaigi.quiz.feature.staff.components.staffDividerColor
import com.droidkaigi.quiz.feature.staff.truncateForDialog

@Composable
fun StaffQuizScreen(
    folderId: String,
    folderName: String,
    folderDescription: String,
    viewModel: StaffQuizViewModel = viewModel(key = folderId) { StaffQuizViewModel(folderId) },
) {
    val state by viewModel.uiState.collectAsState()
    var questionToDelete by remember { mutableStateOf<Question?>(null) }
    val draft = state.editorDraft

    Row(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight(),
        ) {
            StaffQuizContent(
                quizTitle = folderName.ifBlank { state.quizSet?.title ?: "クイズ内容" },
                quizSubtitle = folderDescription,
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
            if (draft != null) {
                // Dim the list and consume pointers so the editor owns focus (old AlertDialog was modal).
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f))
                        .clickable(
                            indication = null,
                            interactionSource = remember { MutableInteractionSource() },
                            onClick = {},
                        ),
                )
            }
        }
        if (draft != null) {
            StaffQuestionEditorPanel(
                draft = draft,
                isNew = state.isNewQuestion,
                onDraftChange = { viewModel.onIntent(StaffQuizIntent.UpdateEditorDraft(it)) },
                onDismiss = { viewModel.onIntent(StaffQuizIntent.DismissEditor) },
                onSave = { viewModel.onIntent(StaffQuizIntent.SaveEditor) },
            )
        }
    }

    val deleteTarget = questionToDelete
    if (deleteTarget != null) {
        StaffConfirmDialog(
            title = "問題を削除",
            message = "「${truncateForDialog(deleteTarget.prompt)}」を削除しますか？\nこの操作は取り消せません。",
            confirmLabel = "削除",
            destructive = true,
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
    quizSubtitle: String?,
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
    StaffContentPane(modifier = modifier.fillMaxSize()) {
        StaffSectionHeader(title = quizTitle ?: "クイズ内容", subtitle = quizSubtitle) {
            StaffTextButton(text = "再読込", icon = Icons.Default.Refresh, onClick = onRefresh)
            StaffFilledButton(text = "問題を追加", icon = Icons.Default.Add, onClick = onAddQuestion)
        }
        Spacer(modifier = Modifier.height(QuizTokens.spacingExtraLarge))
        if (questions.isNotEmpty()) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = QuizTokens.spacingSmall),
            ) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp),
                )
                Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
                Text(
                    text = "ドラッグで問題の出題順を並び替え",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
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
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Icon(
                        imageVector = Icons.Default.Quiz,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(48.dp),
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    Text(
                        text = "問題がありません",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingLarge))
                    StaffFilledButton(text = "問題を追加", icon = Icons.Default.Add, onClick = onAddQuestion)
                }
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
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 192.dp)
            .shadow(if (isDragging) 8.dp else 0.dp, RoundedCornerShape(QuizTokens.cornerMedium))
            .clip(RoundedCornerShape(QuizTokens.cornerMedium))
            .background(MaterialTheme.colorScheme.secondaryContainer)
            .border(1.dp, staffDividerColor(), RoundedCornerShape(QuizTokens.cornerMedium))
            .padding(QuizTokens.spacingMedium),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Default.DragIndicator,
            contentDescription = "並び替え",
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            modifier = dragHandleModifier
                .padding(top = 4.dp)
                .size(QuizTokens.spacingLarge),
        )
        Spacer(modifier = Modifier.width(QuizTokens.spacingMedium))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Q$index",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
                Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
                StaffQuestionTypeChip(
                    text = questionTypeLabel(question),
                    type = questionTypeTone(question),
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "ID: ${question.id}",
                style = MaterialTheme.typography.bodySmall,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.width(QuizTokens.spacingMedium))
        StaffQuestionMarkdownViewport(
            prompt = question.prompt,
            explanation = question.explanationMarkdown,
            modifier = Modifier
                .weight(1.5f)
                .height(160.dp),
        )
        Spacer(modifier = Modifier.width(QuizTokens.spacingMedium))
        Column(
            modifier = Modifier.weight(1.5f),
            horizontalAlignment = Alignment.End,
        ) {
            StaffCorrectAnswerChip(question = question)
            Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
            Row {
                StaffTextButton(text = "編集", icon = null, onClick = onEdit)
                StaffTextButton(
                    text = "削除",
                    icon = null,
                    onClick = onDelete,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}

@Composable
private fun StaffCorrectAnswerChip(question: Question) {
    val answer = correctAnswerText(question)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
            .padding(horizontal = QuizTokens.spacingSmall, vertical = 4.dp),
        horizontalAlignment = Alignment.End,
    ) {
        Row(verticalAlignment = Alignment.Top) {
            Icon(
                imageVector = if (question is Reorder) {
                    Icons.Default.FormatListNumbered
                } else {
                    Icons.Default.CheckCircle
                },
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .size(14.dp),
            )
            Spacer(modifier = Modifier.width(4.dp))
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = if (question is Reorder) "正解順:" else "正解:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary,
                )
                Text(
                    text = answer,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.9f),
                    softWrap = true,
                    maxLines = 4,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
private fun StaffQuestionMarkdownViewport(prompt: String, explanation: String, modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(QuizTokens.cornerSmall))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.16f))
            .border(1.dp, staffDividerColor(0.12f), RoundedCornerShape(QuizTokens.cornerSmall))
            .verticalScroll(scrollState)
            .padding(horizontal = QuizTokens.spacingSmall, vertical = 6.dp),
    ) {
        QuizMarkdownText(prompt)
        if (explanation.isNotBlank()) {
            Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
            Text(
                text = explanation,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private fun questionTypeTone(question: Question): StaffQuestionTypeTone = when (question) {
    is SingleChoice -> StaffQuestionTypeTone.SingleChoice
    is MultipleChoice -> StaffQuestionTypeTone.MultipleChoice
    is Reorder -> StaffQuestionTypeTone.Reorder
}
