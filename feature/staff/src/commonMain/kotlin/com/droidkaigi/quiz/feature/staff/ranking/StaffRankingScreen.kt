package com.droidkaigi.quiz.feature.staff.ranking

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.ui.components.QuizPrimaryButton
import com.droidkaigi.quiz.core.ui.components.QuizRankingRow
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

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
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(QuizTokens.spacingLarge),
    ) {
        Text(
            text = "本日のランキング",
            style = MaterialTheme.typography.headlineMedium,
        )
        Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
        QuizPrimaryButton(
            text = "更新",
            onClick = onRefresh,
            enabled = !isLoading,
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(QuizTokens.spacingLarge))
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
            entries.isEmpty() -> {
                Text(
                    text = "本日のスコアはまだありません",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
                ) {
                    itemsIndexed(entries) { index, entry ->
                        QuizRankingRow(
                            rank = index + 1,
                            nickname = entry.nickname,
                            score = entry.score,
                            highlighted = false,
                        )
                    }
                }
            }
        }
    }
}
