package jp.co.yumemi.quiz.droidkaigi.feature.staff.folders

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Publish
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizTextField
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import jp.co.yumemi.quiz.droidkaigi.feature.staff.StaffConfirmDialog
import jp.co.yumemi.quiz.droidkaigi.feature.staff.StaffShellIntent
import jp.co.yumemi.quiz.droidkaigi.feature.staff.StaffShellUiState
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffActivePill
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffHorizontalDivider
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffOutlinedButton
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffVerticalDivider

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

    Row(modifier = modifier.fillMaxHeight()) {
        Column(
            modifier = Modifier
                .width(QuizTokens.staffSidebarWidth)
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceContainer),
        ) {
            StaffFolderSidebarHeader(onAddClick = { onIntent(StaffShellIntent.ShowCreateFolderDialog) })
            StaffHorizontalDivider(alpha = 0.1f)
            StaffFolderList(
                state = state,
                onIntent = onIntent,
                modifier = Modifier.weight(1f),
            )
            StaffHorizontalDivider(alpha = 0.1f)
            StaffOutlinedButton(
                text = "参加者向けに公開",
                icon = Icons.Default.Publish,
                onClick = { showPublishConfirm = true },
                enabled = selectedFolder != null && !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(QuizTokens.spacingMedium),
            )
        }
        StaffVerticalDivider(alpha = 0.15f)
    }

    StaffFolderDialogs(
        state = state,
        selectedFolder = selectedFolder,
        showPublishConfirm = showPublishConfirm,
        onDismissPublishConfirm = { showPublishConfirm = false },
        onIntent = onIntent,
        newFolderName = newFolderName,
        onNewFolderNameChange = onNewFolderNameChange,
        newFolderDescription = newFolderDescription,
        onNewFolderDescriptionChange = onNewFolderDescriptionChange,
    )
}

@Composable
private fun StaffFolderSidebarHeader(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = QuizTokens.spacingLarge,
                end = QuizTokens.spacingSmall,
                top = QuizTokens.spacingMedium,
                bottom = QuizTokens.spacingMedium,
            ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "クイズフォルダ",
                style = MaterialTheme.typography.titleMedium,
            )
            Text(
                text = "管理中のフォルダ",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        IconButton(onClick = onAddClick) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "フォルダを追加",
                tint = MaterialTheme.colorScheme.primary,
            )
        }
    }
}

@Composable
private fun StaffFolderList(
    state: StaffShellUiState,
    onIntent: (StaffShellIntent) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        when {
            state.isLoading -> CircularProgressIndicator(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(QuizTokens.spacingLarge),
            )

            state.folders.isEmpty() -> Text(
                text = if (state.errorMessage == null) {
                    "フォルダがありません。＋から追加してください。"
                } else {
                    "フォルダを読み込めませんでした。"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (state.errorMessage == null) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.error
                },
                modifier = Modifier.padding(QuizTokens.spacingMedium),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxHeight(),
                contentPadding = PaddingValues(vertical = QuizTokens.spacingSmall),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                items(state.folders, key = { it.id }) { folder ->
                    StaffFolderRow(
                        folder = folder,
                        selected = folder.id == state.selectedFolderId,
                        isActive = folder.id == state.activeFolderId,
                        onClick = { onIntent(StaffShellIntent.SelectFolder(folder.id)) },
                        onEdit = { onIntent(StaffShellIntent.ShowEditFolderDialog(folder.id)) },
                        onDelete = { onIntent(StaffShellIntent.RequestDeleteFolder(folder.id)) },
                    )
                }
            }
        }
        if (state.errorMessage != null && state.folders.isNotEmpty()) {
            Text(
                text = state.errorMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .padding(QuizTokens.spacingMedium),
            )
        }
    }
}

@Composable
private fun StaffFolderDialogs(
    state: StaffShellUiState,
    selectedFolder: QuizFolder?,
    showPublishConfirm: Boolean,
    onDismissPublishConfirm: () -> Unit,
    onIntent: (StaffShellIntent) -> Unit,
    newFolderName: String,
    onNewFolderNameChange: (String) -> Unit,
    newFolderDescription: String,
    onNewFolderDescriptionChange: (String) -> Unit,
) {
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
                onDismissPublishConfirm()
                onIntent(StaffShellIntent.PublishSelectedFolder)
            },
            onDismiss = onDismissPublishConfirm,
        )
    }

    if (state.showCreateFolderDialog) {
        StaffFolderCreateDialog(
            name = newFolderName,
            onNameChange = onNewFolderNameChange,
            description = newFolderDescription,
            onDescriptionChange = onNewFolderDescriptionChange,
            onConfirm = { onIntent(StaffShellIntent.CreateFolder(newFolderName, newFolderDescription)) },
            onDismiss = { onIntent(StaffShellIntent.DismissCreateFolderDialog) },
        )
    }

    val editingFolder = state.editingFolder
    if (editingFolder != null) {
        StaffFolderEditDialog(
            folder = editingFolder,
            onConfirm = { name, description ->
                onIntent(StaffShellIntent.UpdateFolder(editingFolder.id, name, description))
            },
            onDismiss = { onIntent(StaffShellIntent.DismissEditFolderDialog) },
        )
    }

    val deletingFolder = state.deletingFolder
    if (deletingFolder != null) {
        val isActive = deletingFolder.id == state.activeFolderId
        StaffConfirmDialog(
            title = "フォルダを削除",
            message = buildString {
                append("「${deletingFolder.displayName}」を削除しますか？\n")
                append("問題とランキングも削除されます。この操作は取り消せません。")
                if (isActive) {
                    append("\n公開中のフォルダです。削除すると別のフォルダが公開対象になります。")
                }
            },
            confirmLabel = "削除",
            onConfirm = { onIntent(StaffShellIntent.ConfirmDeleteFolder) },
            onDismiss = { onIntent(StaffShellIntent.DismissDeleteFolderDialog) },
            destructive = true,
        )
    }
}

@Composable
private fun StaffFolderCreateDialog(
    name: String,
    onNameChange: (String) -> Unit,
    description: String,
    onDescriptionChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
        title = { Text(text = "フォルダを追加", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium)) {
                QuizTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = "名前（日・難易度など）",
                )
                QuizTextField(
                    value = description,
                    onValueChange = onDescriptionChange,
                    label = "説明",
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = name.isNotBlank()) {
                Text(text = "作成", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "キャンセル",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun StaffFolderEditDialog(
    folder: QuizFolder,
    onConfirm: (name: String, description: String) -> Unit,
    onDismiss: () -> Unit,
) {
    var name by remember(folder.id) { mutableStateOf(folder.name) }
    var description by remember(folder.id) { mutableStateOf(folder.description) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
        title = { Text(text = "フォルダを編集", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium)) {
                QuizTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = "名前（日・難易度など）",
                )
                QuizTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = "説明",
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onConfirm(name, description) },
                enabled = name.isNotBlank(),
            ) {
                Text(text = "保存", style = MaterialTheme.typography.labelLarge)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = "キャンセル",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Composable
private fun StaffFolderRow(
    folder: QuizFolder,
    selected: Boolean,
    isActive: Boolean,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    val background = if (selected) {
        MaterialTheme.colorScheme.secondaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceContainer
    }
    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onSecondaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }
    val shape = RoundedCornerShape(topEnd = QuizTokens.cornerSmall, bottomEnd = QuizTokens.cornerSmall)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
            .clip(shape)
            .background(background)
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        // 4dp leading bar marks the selected folder, per the design system's sidebar rule.
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(48.dp)
                .background(if (selected) MaterialTheme.colorScheme.primary else Color.Transparent),
        )
        Row(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = QuizTokens.spacingMedium,
                    end = if (selected) QuizTokens.spacingSmall else QuizTokens.spacingMedium,
                    top = QuizTokens.spacingSmall,
                    bottom = QuizTokens.spacingSmall,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = null,
                tint = if (selected) MaterialTheme.colorScheme.primary else contentColor,
                modifier = Modifier.size(20.dp),
            )
            Spacer(modifier = Modifier.width(QuizTokens.spacingMedium))
            Text(
                text = folder.displayName,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f),
            )
            // Edit/delete target the selected folder, so affordances only show on that row.
            if (selected) {
                IconButton(onClick = onEdit, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "「${folder.displayName}」の名前・説明を編集",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp),
                    )
                }
                IconButton(onClick = onDelete, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "「${folder.displayName}」を削除",
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
            if (isActive) {
                Spacer(modifier = Modifier.width(QuizTokens.spacingSmall))
                StaffActivePill(text = "公開中")
            }
        }
    }
}
