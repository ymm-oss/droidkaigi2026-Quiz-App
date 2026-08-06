package com.droidkaigi.quiz.staff.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.feature.quiz.quiz.QuizScreen
import com.droidkaigi.quiz.feature.quiz.result.ResultContent
import kotlinx.coroutines.flow.emptyFlow

private enum class PreviewPhase { Loading, Quiz, Result, Error }

@Composable
fun StaffQuizPreviewDialog(
    folderId: String,
    onDismiss: () -> Unit,
) {
    var phase by remember(folderId) { mutableStateOf(PreviewPhase.Loading) }
    var errorMessage by remember(folderId) { mutableStateOf<String?>(null) }
    var previewResult by remember(folderId) { mutableStateOf<QuizResult?>(null) }
    val deps = AppDependencies.shared

    LaunchedEffect(folderId) {
        phase = PreviewPhase.Loading
        errorMessage = null
        previewResult = null
        runCatching {
            val quizSet = deps.getQuizSetForFolderUseCase(folderId)
            require(quizSet.questions.isNotEmpty()) { "プレビューする問題がありません" }
            val session = deps.quizEngine.startSession(
                folderId = folderId,
                quizSet = quizSet,
                nickname = "プレビュー",
                startedAtEpochMillis = deps.instantProvider.nowEpochMillis(),
            )
            deps.sessionHolder.beginSession(session)
            phase = PreviewPhase.Quiz
        }.onFailure { error ->
            errorMessage = error.message ?: "プレビューを開始できませんでした"
            phase = PreviewPhase.Error
        }
    }

    DisposableEffect(folderId) {
        onDispose {
            deps.sessionHolder.clearPlaySession()
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Surface(
            modifier = Modifier
                .width(400.dp)
                .height(760.dp)
                .padding(QuizTokens.spacingMedium),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
            tonalElevation = 6.dp,
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = QuizTokens.spacingSmall),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "参加者プレビュー",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = QuizTokens.spacingMedium),
                    )
                    TextButton(onClick = onDismiss) {
                        Text("閉じる")
                    }
                }
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(
                            horizontal = QuizTokens.spacingLarge,
                            vertical = QuizTokens.spacingSmall,
                        ),
                    contentAlignment = Alignment.Center,
                ) {
                    PhoneFrame {
                        PreviewBody(
                            phase = phase,
                            errorMessage = errorMessage,
                            previewResult = previewResult,
                            onFinished = {
                                previewResult = deps.sessionHolder.lastResult
                                phase = PreviewPhase.Result
                            },
                            onAbandoned = onDismiss,
                            onCloseResult = onDismiss,
                        )
                    }
                }
                Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
            }
        }
    }
}

@Composable
private fun PreviewBody(
    phase: PreviewPhase,
    errorMessage: String?,
    previewResult: QuizResult?,
    onFinished: () -> Unit,
    onAbandoned: () -> Unit,
    onCloseResult: () -> Unit,
) {
    when (phase) {
        PreviewPhase.Loading -> {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                CircularProgressIndicator()
            }
        }

        PreviewPhase.Error -> {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(QuizTokens.spacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Text(
                    text = errorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        }

        PreviewPhase.Quiz -> {
            QuizScreen(
                onFinished = onFinished,
                onAbandoned = onAbandoned,
                leaveRequest = emptyFlow(),
                submitScore = false,
            )
        }

        PreviewPhase.Result -> {
            if (previewResult == null) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = "結果がありません",
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            } else {
                ResultContent(
                    nickname = previewResult.nickname,
                    correctCount = previewResult.correctCount,
                    totalCount = previewResult.totalCount,
                    targetScore = previewResult.score,
                    onGoToRankingClick = onCloseResult,
                    animateScore = false,
                    primaryActionLabel = "閉じる",
                )
            }
        }
    }
}

@Composable
private fun PhoneFrame(content: @Composable () -> Unit) {
    val frameShape = RoundedCornerShape(28.dp)
    Box(
        modifier = Modifier
            .width(360.dp)
            .height(640.dp)
            .clip(frameShape)
            .border(2.dp, MaterialTheme.colorScheme.outlineVariant, frameShape)
            .background(MaterialTheme.colorScheme.surface),
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = 8.dp)
                .size(width = 96.dp, height = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.6f)),
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 20.dp),
        ) {
            content()
        }
    }
}
