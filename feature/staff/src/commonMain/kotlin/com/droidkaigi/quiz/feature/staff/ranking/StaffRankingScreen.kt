package com.droidkaigi.quiz.feature.staff.ranking

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.time.formatCompletedAtLabel
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.feature.staff.components.StaffContentPane
import com.droidkaigi.quiz.feature.staff.components.StaffFilledButton
import com.droidkaigi.quiz.feature.staff.components.StaffHorizontalDivider
import com.droidkaigi.quiz.feature.staff.components.StaffSectionHeader
import com.droidkaigi.quiz.feature.staff.components.staffDividerColor

@Composable
fun StaffRankingScreen(
    folderId: String,
    viewModel: StaffRankingViewModel = viewModel(key = folderId) { StaffRankingViewModel(folderId) },
) {
    val state by viewModel.uiState.collectAsState()
    StaffRankingContent(
        entries = state.entries,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onRefresh = { viewModel.onIntent(StaffRankingIntent.Refresh) },
    )
}

@Composable
fun StaffRankingContent(
    entries: List<RankingEntry>,
    isLoading: Boolean,
    errorMessage: String?,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    StaffContentPane(modifier = modifier.fillMaxSize()) {
        StaffSectionHeader(title = "本日のランキング", subtitle = null) {
            StaffFilledButton(
                text = "更新",
                icon = Icons.Default.Refresh,
                onClick = onRefresh,
                enabled = !isLoading,
            )
        }
        Spacer(modifier = Modifier.height(QuizTokens.spacingExtraLarge))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(QuizTokens.cornerMedium))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .border(1.dp, staffDividerColor(), RoundedCornerShape(QuizTokens.cornerMedium)),
        ) {
            StaffRankingHeaderRow()
            StaffHorizontalDivider()
            when {
                isLoading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(QuizTokens.spacingExtraLarge),
                    contentAlignment = Alignment.Center,
                ) {
                    CircularProgressIndicator()
                }

                errorMessage != null -> StaffRankingNote(text = errorMessage, isError = true)

                entries.isEmpty() -> StaffRankingNote(text = "本日のスコアはまだありません")

                else -> {
                    LazyColumn(modifier = Modifier.weight(1f, fill = false)) {
                        itemsIndexed(entries) { index, entry ->
                            StaffRankingRow(rank = index + 1, entry = entry)
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
        StaffRankingCell(text = "Score", weight = 3f, align = TextAlign.End)
        StaffRankingCell(text = "Completed Time", weight = 3f, align = TextAlign.End)
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
private fun StaffRankingRow(rank: Int, entry: RankingEntry) {
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
            text = "スコア ${entry.score}",
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
