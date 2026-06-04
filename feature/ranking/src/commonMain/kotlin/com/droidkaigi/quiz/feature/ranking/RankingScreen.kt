package com.droidkaigi.quiz.feature.ranking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun RankingScreen(
    onGoHome: () -> Unit,
    viewModel: RankingViewModel = viewModel { RankingViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RankingEvent.NavigateHome -> onGoHome()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(QuizTokens.spacingMedium),
    ) {
        Text(text = "今日のランキング", style = MaterialTheme.typography.headlineMedium)
        if (state.isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
        } else {
            LazyColumn(modifier = Modifier.weight(1f)) {
                itemsIndexed(state.entries, key = { _, e -> "${e.nickname}-${e.completedAtEpochMillis}" }) { index, entry ->
                    AnimatedVisibility(
                        visible = true,
                        enter = fadeIn() + slideInVertically { it / 2 },
                    ) {
                        val highlighted = entry.nickname == state.highlightNickname
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = QuizTokens.spacingSmall)
                                .background(
                                    if (highlighted) QuizTokens.highlight else MaterialTheme.colorScheme.surface,
                                )
                                .padding(QuizTokens.spacingMedium),
                        ) {
                            Text("${index + 1}.", style = MaterialTheme.typography.titleMedium)
                            Column(modifier = Modifier.padding(start = QuizTokens.spacingMedium).weight(1f)) {
                                Text(entry.nickname, style = MaterialTheme.typography.bodyLarge)
                                Text("スコア ${entry.score}")
                            }
                        }
                    }
                }
            }
        }
        Button(
            onClick = { viewModel.onIntent(RankingIntent.GoHome) },
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("ホームに戻る")
        }
    }
}
