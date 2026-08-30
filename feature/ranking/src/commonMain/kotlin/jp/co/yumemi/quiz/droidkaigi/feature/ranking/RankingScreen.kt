package jp.co.yumemi.quiz.droidkaigi.feature.ranking

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
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
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.time.formatCompletedAtLabel
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizHeroTitle
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizPrimaryButton
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizRankingRow
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizScreenBackground
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizSecondaryButton
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizSurfaceCard
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.Res
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.ranking_empty
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.ranking_error_load_failed
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.ranking_go_home
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.ranking_retry
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.ranking_subtitle
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.ranking_title
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.time_unknown
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.quizSafeHorizontalPadding
import org.jetbrains.compose.resources.stringResource

@Composable
fun RankingScreen(onGoHome: () -> Unit, viewModel: RankingViewModel = viewModel { RankingViewModel() }) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                RankingEvent.NavigateHome -> onGoHome()
            }
        }
    }

    val errorMessage = when (val error = state.error) {
        null -> null
        is RankingError.LoadFailed -> error.detail?.takeIf { it.isNotBlank() }
            ?: stringResource(Res.string.ranking_error_load_failed)
    }

    RankingContent(
        entries = state.entries,
        highlightNickname = state.highlightNickname,
        isLoading = state.isLoading,
        errorMessage = errorMessage,
        onRetryClick = { viewModel.onIntent(RankingIntent.Refresh) },
        onGoHomeClick = { viewModel.onIntent(RankingIntent.GoHome) },
    )
}

@Composable
fun RankingContent(
    entries: List<RankingEntry>,
    highlightNickname: String?,
    isLoading: Boolean,
    onGoHomeClick: () -> Unit,
    modifier: Modifier = Modifier,
    errorMessage: String? = null,
    onRetryClick: (() -> Unit)? = null,
) {
    val unknownCompletedAt = stringResource(Res.string.time_unknown)
    QuizScreenBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .quizSafeHorizontalPadding(),
        ) {
            Column(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .fillMaxSize()
                    .widthIn(max = 640.dp)
                    .fillMaxWidth()
                    .padding(horizontal = QuizTokens.spacingLarge)
                    .windowInsetsPadding(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical),
                    )
                    .padding(top = QuizTokens.spacingMedium),
            ) {
                QuizHeroTitle(
                    title = stringResource(Res.string.ranking_title),
                    subtitle = stringResource(Res.string.ranking_subtitle),
                    badge = "RANKING",
                )
                Spacer(modifier = Modifier.height(QuizTokens.spacingLarge))
                if (isLoading) {
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
                        contentPadding = PaddingValues(bottom = QuizTokens.spacingLarge),
                        verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall),
                    ) {
                        if (errorMessage != null) {
                            item(key = "ranking-error") {
                                QuizSurfaceCard {
                                    Text(
                                        text = errorMessage,
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.error,
                                    )
                                    if (onRetryClick != null) {
                                        Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                                        QuizPrimaryButton(
                                            text = stringResource(Res.string.ranking_retry),
                                            onClick = onRetryClick,
                                        )
                                    }
                                }
                            }
                        }
                        itemsIndexed(
                            items = entries,
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
                                    totalCount = entry.totalCount,
                                    highlighted = entry.nickname == highlightNickname,
                                    completedAtLabel = formatCompletedAtLabel(entry.completedAtEpochMillis)
                                        ?: unknownCompletedAt,
                                )
                            }
                        }
                        if (entries.isEmpty() && errorMessage == null) {
                            item {
                                QuizSurfaceCard {
                                    Text(
                                        text = stringResource(Res.string.ranking_empty),
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
                    text = stringResource(Res.string.ranking_go_home),
                    onClick = onGoHomeClick,
                )
                Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
            }
        }
    }
}
