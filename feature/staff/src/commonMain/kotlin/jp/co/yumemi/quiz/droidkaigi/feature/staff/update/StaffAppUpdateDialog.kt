package jp.co.yumemi.quiz.droidkaigi.feature.staff.update

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens

@Composable
fun StaffAppUpdateDialog(state: StaffAppUpdateUiState, onIntent: (StaffAppUpdateIntent) -> Unit) {
    if (!state.showDialog) return
    val release = state.release ?: return
    val notes = release.releaseNotes.trim().ifBlank { null }
    AlertDialog(
        onDismissRequest = {
            if (!state.isDownloading) onIntent(StaffAppUpdateIntent.Dismiss)
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
        title = {
            Text(text = "新しいバージョンがあります", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "スタッフ Desktop ${release.version} が利用できます。\n" +
                        "DMG をダウンロードして Applications に置き換えてください。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (notes != null) {
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    Text(
                        text = notes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                if (state.isDownloading) {
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    val progress = state.downloadProgress
                    if (progress != null) {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth(),
                        )
                    } else {
                        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                    }
                    Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
                    Text(
                        text = "ダウンロード中…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                state.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onIntent(StaffAppUpdateIntent.Download) },
                enabled = !state.isDownloading,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(QuizTokens.cornerSmall),
                contentPadding = PaddingValues(horizontal = QuizTokens.spacingLarge, vertical = 0.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary,
                ),
                elevation = null,
            ) {
                Text(
                    text = if (state.isDownloading) "ダウンロード中" else "ダウンロード",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onIntent(StaffAppUpdateIntent.Dismiss) },
                enabled = !state.isDownloading,
            ) {
                Text(
                    text = "後で",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}
