package com.droidkaigi.quiz.feature.ranking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.ui.components.QuizHeroTitle
import com.droidkaigi.quiz.core.ui.components.QuizRankingRow
import com.droidkaigi.quiz.core.ui.components.QuizScreenBackground
import com.droidkaigi.quiz.core.ui.components.QuizSecondaryButton
import com.droidkaigi.quiz.core.ui.components.QuizSurfaceCard
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

    QuizScreenBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = QuizTokens.spacingLarge),
            ) {
                Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                QuizHeroTitle(
                    title = "今日のランキング",
                    subtitle = "今日のベストスコア",
                    badge = "RANKING",
                )
                Spacer(modifier = Modifier.height(QuizTokens.spacingLarge))
                if (state.isLoading) {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center,
                    ) {
                        QuizSurfaceCard {
                            Box(
                                modifier = Modifier.fillMaxWidth(),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
                    ) {
                        itemsIndexed(
                            items = state.entries,
                            key = { _, e -> "${e.nickname}-${e.completedAtEpochMillis}" },
                        ) { index, entry ->
                            AnimatedVisibility(
                                visible = true,
                                enter = fadeIn() + slideInVertically { it / 2 },
                            ) {
                                QuizRankingRow(
                                    rank = index + 1,
                                    nickname = entry.nickname,
                                    score = entry.score,
                                    highlighted = entry.nickname == state.highlightNickname,
                                )
                            }
                        }
                        if (state.entries.isEmpty()) {
                            item {
                                QuizSurfaceCard {
                                    Text(
                                        text = "まだエントリーがありません",
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(QuizTokens.spacingLarge))
                QuizSecondaryButton(
                    text = "ホームに戻る",
                    onClick = { viewModel.onIntent(RankingIntent.GoHome) },
                )
                Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
            }
        }
    }
}
