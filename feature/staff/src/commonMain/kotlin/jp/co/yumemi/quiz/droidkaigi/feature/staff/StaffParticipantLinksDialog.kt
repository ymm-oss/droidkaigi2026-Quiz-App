package jp.co.yumemi.quiz.droidkaigi.feature.staff

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.OpenInNew
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import jp.co.yumemi.quiz.droidkaigi.feature.staff.components.StaffTextButton

@Composable
fun StaffParticipantLinksDialog(
    webAppUrl: String,
    androidDesktopReleasesUrl: String,
    onDismiss: () -> Unit,
) {
    val uriHandler = LocalUriHandler.current
    @Suppress("DEPRECATION")
    val clipboard = LocalClipboardManager.current
    var copiedUrl by remember { mutableStateOf<String?>(null) }
    var openError by remember { mutableStateOf<String?>(null) }

    fun openUrl(url: String) {
        openError = null
        try {
            uriHandler.openUri(url)
        } catch (_: Exception) {
            openError = "リンクを開けませんでした"
        }
    }

    fun copyUrl(url: String) {
        clipboard.setText(AnnotatedString(url))
        copiedUrl = url
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        shape = RoundedCornerShape(QuizTokens.cornerMedium),
        title = {
            Text(text = "参加者アプリ", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = "会場で配布するリンクです。コピーするか、ブラウザで開けます。",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Spacer(modifier = Modifier.height(QuizTokens.spacingLarge))
                ParticipantLinkRow(
                    title = "Web（ブラウザ）",
                    url = webAppUrl,
                    copied = copiedUrl == webAppUrl,
                    onCopy = { copyUrl(webAppUrl) },
                    onOpen = { openUrl(webAppUrl) },
                )
                Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                ParticipantLinkRow(
                    title = "Android / Desktop",
                    url = androidDesktopReleasesUrl,
                    copied = copiedUrl == androidDesktopReleasesUrl,
                    onCopy = { copyUrl(androidDesktopReleasesUrl) },
                    onOpen = { openUrl(androidDesktopReleasesUrl) },
                )
                openError?.let { message ->
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
            StaffTextButton(
                text = "閉じる",
                icon = null,
                onClick = onDismiss,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        },
    )
}

@Composable
private fun ParticipantLinkRow(
    title: String,
    url: String,
    copied: Boolean,
    onCopy: () -> Unit,
    onOpen: () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = url,
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.heightIn(min = 20.dp),
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
        ) {
            StaffTextButton(
                text = if (copied) "コピー済み" else "コピー",
                icon = Icons.Outlined.ContentCopy,
                onClick = onCopy,
            )
            StaffTextButton(
                text = "開く",
                icon = Icons.AutoMirrored.Outlined.OpenInNew,
                onClick = onOpen,
            )
        }
    }
}
