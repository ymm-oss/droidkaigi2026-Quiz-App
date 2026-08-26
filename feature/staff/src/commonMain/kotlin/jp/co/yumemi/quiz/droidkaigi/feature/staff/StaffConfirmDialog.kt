package jp.co.yumemi.quiz.droidkaigi.feature.staff

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens

@Composable
fun StaffConfirmDialog(
    title: String,
    message: String,
    confirmLabel: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    destructive: Boolean = false,
    confirmLoading: Boolean = false,
    errorMessage: String? = null,
) {
    AlertDialog(
        onDismissRequest = { if (!confirmLoading) onDismiss() },
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
        title = { Text(text = title, style = MaterialTheme.typography.titleLarge) },
        text = {
            Column {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                if (confirmLoading) {
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    Text(
                        text = "処理中…",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                errorMessage?.let { messageText ->
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    Text(
                        text = messageText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = !confirmLoading,
                modifier = Modifier.height(36.dp),
                shape = RoundedCornerShape(QuizTokens.cornerSmall),
                contentPadding = PaddingValues(horizontal = QuizTokens.spacingLarge, vertical = 0.dp),
                colors = if (destructive) {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error,
                        contentColor = MaterialTheme.colorScheme.onError,
                    )
                } else {
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary,
                    )
                },
                elevation = null,
            ) {
                if (confirmLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = if (destructive) {
                            MaterialTheme.colorScheme.onError
                        } else {
                            MaterialTheme.colorScheme.onPrimary
                        },
                        strokeWidth = 2.dp,
                    )
                } else {
                    Text(text = confirmLabel, style = MaterialTheme.typography.labelLarge)
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss, enabled = !confirmLoading) {
                Text(
                    text = "キャンセル",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

internal fun truncateForDialog(text: String, maxLength: Int = 80): String =
    if (text.length <= maxLength) text else text.take(maxLength) + "…"
