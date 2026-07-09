package com.droidkaigi.quiz.feature.staff.folders

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.ui.components.QuizPrimaryButton
import com.droidkaigi.quiz.core.ui.components.QuizTextField
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.feature.staff.StaffConfirmDialog
import com.droidkaigi.quiz.feature.staff.StaffShellIntent
import com.droidkaigi.quiz.feature.staff.StaffShellUiState

@Composable
fun StaffFolderSidebar(
    state: StaffShellUiState,
    onIntent: (StaffShellIntent) -> Unit,
    newFolderName: String,
    onNewFolderNameChange: (String) -> Unit,
    newFolderDescription: String,
    onNewFolderDescriptionChange: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var showPublishConfirm by remember { mutableStateOf(false) }
    val selectedFolder = state.folders.find { it.id == state.selectedFolderId }

    Surface(
        modifier = modifier
            .width(260.dp)
            .fillMaxHeight(),
        tonalElevation = QuizTokens.spacingSmall,
    ) {
        Column(
            modifier = Modifier.padding(QuizTokens.spacingMedium),
            verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
        ) {
            Text(
                text = "フォルダ",
                style = MaterialTheme.typography.titleMedium,
            )
            IconButton(onClick = { onIntent(StaffShellIntent.ShowCreateFolderDialog) }) {
                Icon(Icons.Default.Add, contentDescription = "フォルダを追加")
            }
            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
            } else {
                if (state.errorMessage != null) {
                    Text(
                        text = state.errorMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                if (state.folders.isEmpty()) {
                    Text(
                        text = if (state.errorMessage == null) {
                            "フォルダがありません。＋から追加してください。"
                        } else {
                            "フォルダを読み込めませんでした。"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
                        items(state.folders, key = { it.id }) { folder ->
                            StaffFolderRow(
                                folder = folder,
                                selected = folder.id == state.selectedFolderId,
                                isActive = folder.id == state.activeFolderId,
                                onClick = { onIntent(StaffShellIntent.SelectFolder(folder.id)) },
                            )
                        }
                    }
                }
            }
            QuizPrimaryButton(
                text = "参加者向けに公開",
                onClick = { showPublishConfirm = true },
                enabled = selectedFolder != null && !state.isLoading,
            )
        }
    }

    if (showPublishConfirm && selectedFolder != null) {
        val folder = selectedFolder
        val alreadyActive = folder.id == state.activeFolderId
        StaffConfirmDialog(
            title = "参加者向けに公開",
            message = if (alreadyActive) {
                "「${folder.displayName}」はすでに公開中です。再度公開しますか？"
            } else {
                "「${folder.displayName}」を参加者アプリに公開しますか？\n公開中のフォルダは切り替わります。"
            },
            confirmLabel = "公開",
            onConfirm = {
                showPublishConfirm = false
                onIntent(StaffShellIntent.PublishSelectedFolder)
            },
            onDismiss = { showPublishConfirm = false },
        )
    }

    if (state.showCreateFolderDialog) {
        AlertDialog(
            onDismissRequest = { onIntent(StaffShellIntent.DismissCreateFolderDialog) },
            title = { Text("フォルダを追加") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium)) {
                    QuizTextField(
                        value = newFolderName,
                        onValueChange = onNewFolderNameChange,
                        label = "名前（日・難易度など）",
                    )
                    QuizTextField(
                        value = newFolderDescription,
                        onValueChange = onNewFolderDescriptionChange,
                        label = "説明",
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onIntent(StaffShellIntent.CreateFolder(newFolderName, newFolderDescription))
                    },
                ) {
                    Text("作成")
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(StaffShellIntent.DismissCreateFolderDialog) }) {
                    Text("キャンセル")
                }
            },
        )
    }
}

@Composable
private fun StaffFolderRow(folder: QuizFolder, selected: Boolean, isActive: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        color = if (selected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
    ) {
        Column(modifier = Modifier.padding(QuizTokens.spacingMedium)) {
            Text(
                text = folder.displayName,
                style = MaterialTheme.typography.titleSmall,
            )
            if (folder.description.isNotBlank()) {
                Text(
                    text = folder.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            if (isActive) {
                Text(
                    text = "公開中",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary,
                )
            }
        }
    }
}
