package jp.co.yumemi.quiz.droidkaigi.feature.staff.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.formatCompletedAtLabel
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import jp.co.yumemi.quiz.droidkaigi.feature.staff.StaffConfirmDialog
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffContentPane
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffHorizontalDivider
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffOutlinedButton
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffSectionHeader
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffTextButton
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.staffDividerColor
import jp.co.yumemi.quiz.droidkaigi.feature.staff.truncateForDialog

@Composable
fun StaffRankingScreen(
    folderId: String,
    viewModel: StaffRankingViewModel = viewModel(key = folderId) { StaffRankingViewModel(folderId) },
) {
    val state by viewModel.uiState.collectAsState()
    var entryToDelete by remember(folderId) { mutableStateOf<RankingEntry?>(null) }
    var deleteConfirmed by remember(folderId) { mutableStateOf(false) }
    var showClearTodayConfirm by remember(folderId) { mutableStateOf(false) }
    var clearTodayConfirmed by remember(folderId) { mutableStateOf(false) }

    StaffRankingContent(
        entries = state.entries,
        isLoading = state.isLoading,
        isMutating = state.isMutating,
        loadError = state.loadError,
        mutationError = state.mutationError,
        reloadWarning = state.reloadWarning,
        onRetryLoad = { viewModel.onIntent(StaffRankingIntent.Refresh) },
        onRequestDeleteEntry = {
            viewModel.onIntent(StaffRankingIntent.ClearMutationFeedback)
            deleteConfirmed = false
            entryToDelete = it
        },
        onRequestClearToday = {
            viewModel.onIntent(StaffRankingIntent.ClearMutationFeedback)
            clearTodayConfirmed = false
            showClearTodayConfirm = true
        },
    )

    StaffRankingConfirmDialogs(
        deleteTarget = entryToDelete,
        showClearTodayConfirm = showClearTodayConfirm,
        isMutating = state.isMutating,
        mutationError = state.mutationError,
        onDeleteConfirm = { entryId ->
            deleteConfirmed = true
            viewModel.onIntent(StaffRankingIntent.DeleteEntry(entryId))
        },
        onDeleteDismiss = {
            entryToDelete = null
            deleteConfirmed = false
        },
        onClearTodayConfirm = {
            clearTodayConfirmed = true
            viewModel.onIntent(StaffRankingIntent.ClearToday)
        },
        onClearTodayDismiss = {
            showClearTodayConfirm = false
            clearTodayConfirmed = false
        },
        deleteConfirmed = deleteConfirmed,
        clearTodayConfirmed = clearTodayConfirmed,
        onDeleteMutationFinished = {
            if (state.mutationError == null) {
                entryToDelete = null
                deleteConfirmed = false
            }
        },
        onClearTodayMutationFinished = {
            if (state.mutationError == null) {
                showClearTodayConfirm = false
                clearTodayConfirmed = false
            }
        },
    )
}

@Composable
private fun StaffRankingConfirmDialogs(
    deleteTarget: RankingEntry?,
    showClearTodayConfirm: Boolean,
    isMutating: Boolean,
    mutationError: String?,
    onDeleteConfirm: (String) -> Unit,
    onDeleteDismiss: () -> Unit,
    onClearTodayConfirm: () -> Unit,
    onClearTodayDismiss: () -> Unit,
    deleteConfirmed: Boolean,
    clearTodayConfirmed: Boolean,
    onDeleteMutationFinished: () -> Unit,
    onClearTodayMutationFinished: () -> Unit,
) {
    if (deleteTarget != null) {
        StaffRankingDeleteConfirmDialog(
            deleteTarget = deleteTarget,
            isMutating = isMutating,
            deleteConfirmed = deleteConfirmed,
            mutationError = mutationError,
            onConfirm = { onDeleteConfirm(deleteTarget.id) },
            onDismiss = onDeleteDismiss,
        )
    }

    if (showClearTodayConfirm) {
        StaffRankingClearTodayConfirmDialog(
            isMutating = isMutating,
            clearTodayConfirmed = clearTodayConfirmed,
            mutationError = mutationError,
            onConfirm = onClearTodayConfirm,
            onDismiss = onClearTodayDismiss,
        )
    }

    LaunchedEffect(isMutating, mutationError, deleteConfirmed) {
        if (deleteConfirmed && !isMutating) {
            onDeleteMutationFinished()
        }
    }

    LaunchedEffect(isMutating, mutationError, clearTodayConfirmed) {
        if (clearTodayConfirmed && !isMutating) {
            onClearTodayMutationFinished()
        }
    }
}

@Composable
private fun StaffRankingDeleteConfirmDialog(
    deleteTarget: RankingEntry,
    isMutating: Boolean,
    deleteConfirmed: Boolean,
    mutationError: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    StaffConfirmDialog(
        title = "ランキングを削除",
        message = "「${truncateForDialog(deleteTarget.nickname)}」のスコアを削除しますか？\nこの操作は取り消せません。",
        confirmLabel = "削除",
        confirmLoading = isMutating && deleteConfirmed,
        errorMessage = if (deleteConfirmed && !isMutating) mutationError else null,
        onConfirm = onConfirm,
        onDismiss = {
            if (!isMutating) {
                onDismiss()
            }
        },
        destructive = true,
    )
}

@Composable
private fun StaffRankingClearTodayConfirmDialog(
    isMutating: Boolean,
    clearTodayConfirmed: Boolean,
    mutationError: String?,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    StaffConfirmDialog(
        title = "本日のランキングをすべて削除",
        message = "本日のランキングをすべて削除しますか？\nこの操作は取り消せません。",
        confirmLabel = "すべて削除",
        confirmLoading = isMutating && clearTodayConfirmed,
        errorMessage = if (clearTodayConfirmed && !isMutating) mutationError else null,
        onConfirm = onConfirm,
        onDismiss = {
            if (!isMutating) {
                onDismiss()
            }
        },
        destructive = true,
    )
}

@Composable
fun StaffRankingContent(
    entries: List<RankingEntry>,
    isLoading: Boolean,
    loadError: String?,
    modifier: Modifier = Modifier,
    isMutating: Boolean = false,
    mutationError: String? = null,
    reloadWarning: String? = null,
    onRetryLoad: (() -> Unit)? = null,
    onRequestDeleteEntry: (RankingEntry) -> Unit = {},
    onRequestClearToday: () -> Unit = {},
) {
    val actionsEnabled = !isLoading && !isMutating
    StaffContentPane(modifier = modifier.fillMaxSize()) {
        StaffSectionHeader(title = "本日のランキング", subtitle = null) {
            StaffOutlinedButton(
                text = "すべて削除",
                icon = Icons.Default.DeleteSweep,
                onClick = onRequestClearToday,
                enabled = actionsEnabled && entries.isNotEmpty(),
                destructive = true,
            )
        }
        Spacer(modifier = Modifier.height(QuizTokens.spacingExtraLarge))
        if (isMutating) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
            Text(
                text = "処理中…",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(bottom = QuizTokens.spacingMedium),
            )
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(QuizTokens.cornerMedium))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(1.dp, staffDividerColor(), RoundedCornerShape(QuizTokens.cornerMedium)),
        ) {
            StaffRankingHeaderRow()
            StaffHorizontalDivider()
            mutationError?.let { message ->
                StaffRankingNote(text = message, isError = true)
                StaffHorizontalDivider(alpha = 0.15f)
            }
            reloadWarning?.let { message ->
                StaffRankingNote(text = message, isError = true)
                StaffHorizontalDivider(alpha = 0.15f)
            }
            when {
                isLoading && entries.isEmpty() -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QuizTokens.spacingExtraLarge),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                entries.isEmpty() && loadError != null ->
                    StaffRankingLoadError(message = loadError, onRetry = onRetryLoad)

                entries.isEmpty() -> StaffRankingNote(text = "本日のスコアはまだありません")

                else -> {
                    if (loadError != null) {
                        StaffRankingLoadError(message = loadError, onRetry = onRetryLoad)
                        StaffHorizontalDivider(alpha = 0.15f)
                    }
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        itemsIndexed(
                            items = entries,
                            key = { _, entry ->
                                entry.id.ifBlank { "${entry.nickname}-${entry.completedAtEpochMillis}" }
                            },
                        ) { index, entry ->
                            StaffRankingRow(
                                rank = index + 1,
                                entry = entry,
                                deleteEnabled = actionsEnabled && entry.id.isNotBlank(),
                                onDelete = { onRequestDeleteEntry(entry) },
                            )
                            StaffHorizontalDivider(alpha = 0.15f)
                        }
                    }
                    StaffRankingNote(text = "これ以上のデータはありません")
                }
            }
        }
    }
}

@Composable
private fun StaffRankingHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .padding(horizontal = QuizTokens.spacingLarge, vertical = QuizTokens.spacingMedium),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        StaffRankingCell(text = "Rank", weight = 2f)
        StaffRankingCell(text = "Nickname", weight = 4f)
        StaffRankingCell(text = "正解率", weight = 3f, align = TextAlign.End)
        StaffRankingCell(text = "Completed Time", weight = 3f, align = TextAlign.End)
        StaffRankingCell(text = "Actions", weight = 1.5f, align = TextAlign.End)
    }
}

@Composable
private fun RowScope.StaffRankingCell(text: String, weight: Float, align: TextAlign = TextAlign.Start) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        textAlign = align,
        modifier = Modifier.weight(weight),
    )
}

@Composable
private fun StaffRankingRow(rank: Int, entry: RankingEntry, deleteEnabled: Boolean, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(QuizTokens.staffTableRowHeight)
            .padding(horizontal = QuizTokens.spacingLarge),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(
            modifier = Modifier.weight(2f),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (rank == 1) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primaryContainer,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
            }
            Text(
                text = rank.toString(),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = if (rank <= 3) {
                    MaterialTheme.colorScheme.primaryContainer
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
            )
        }
        Text(
            text = entry.nickname,
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(4f),
        )
        Text(
            text = "${entry.score}%",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(3f),
        )
        Text(
            text = formatCompletedAtLabel(entry.completedAtEpochMillis) ?: "不明",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            modifier = Modifier.weight(3f),
        )
        Box(
            modifier = Modifier.weight(1.5f),
            contentAlignment = Alignment.CenterEnd,
        ) {
            IconButton(
                onClick = onDelete,
                enabled = deleteEnabled,
                modifier = Modifier.size(36.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "${entry.nickname}のランキングを削除",
                    tint = if (deleteEnabled) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                    },
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}

@Composable
private fun StaffRankingLoadError(message: String, onRetry: (() -> Unit)?) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        StaffRankingNote(text = message, isError = true)
        if (onRetry != null) {
            StaffTextButton(
                text = "再試行",
                icon = null,
                onClick = onRetry,
            )
            Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
        }
    }
}

@Composable
private fun StaffRankingNote(text: String, isError: Boolean = false) {
    Text(
        text = text,
        style = MaterialTheme.typography.bodySmall,
        fontStyle = if (isError) FontStyle.Normal else FontStyle.Italic,
        color = if (isError) {
            MaterialTheme.colorScheme.error
        } else {
            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
        },
        textAlign = TextAlign.Center,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = QuizTokens.spacingLarge, vertical = QuizTokens.spacingExtraLarge),
    )
}
