package com.droidkaigi.quiz.feature.quiz.result

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.ui.components.QuizHeroTitle
import com.droidkaigi.quiz.core.ui.components.QuizPrimaryButton
import com.droidkaigi.quiz.core.ui.components.QuizScreenBackground
import com.droidkaigi.quiz.core.ui.components.QuizSurfaceCard
import com.droidkaigi.quiz.core.ui.generated.resources.Res
import com.droidkaigi.quiz.core.ui.generated.resources.result_correct_count
import com.droidkaigi.quiz.core.ui.generated.resources.result_go_ranking
import com.droidkaigi.quiz.core.ui.generated.resources.result_score_label
import com.droidkaigi.quiz.core.ui.generated.resources.result_section
import com.droidkaigi.quiz.core.ui.generated.resources.result_subtitle
import com.droidkaigi.quiz.core.ui.generated.resources.result_title
import com.droidkaigi.quiz.core.ui.theme.QuizMotion
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
fun ResultScreen(onGoToRanking: () -> Unit, viewModel: ResultViewModel = viewModel { ResultViewModel() }) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ResultEvent.NavigateToRanking -> onGoToRanking()
            }
        }
    }

    ResultContent(
        nickname = state.nickname,
        correctCount = state.correctCount,
        totalCount = state.totalCount,
        targetScore = state.targetScore,
        onGoToRankingClick = { viewModel.onIntent(ResultIntent.GoToRanking) },
    )
}

@Composable
fun ResultContent(
    nickname: String,
    correctCount: Int,
    totalCount: Int,
    targetScore: Int,
    onGoToRankingClick: () -> Unit,
    modifier: Modifier = Modifier,
    animateScore: Boolean = true,
    primaryActionLabel: String? = null,
) {
    val displayedScore = if (animateScore) {
        QuizMotion.animateScore(targetScore)
    } else {
        targetScore
    }
    val actionLabel = primaryActionLabel ?: stringResource(Res.string.result_go_ranking)

    QuizScreenBackground(modifier = modifier) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            ConfettiBackground(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 120.dp),
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .safeDrawingPadding()
                    .padding(horizontal = QuizTokens.spacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingExtraLarge),
            ) {
                QuizHeroTitle(
                    title = stringResource(Res.string.result_title),
                    subtitle = stringResource(Res.string.result_subtitle, nickname),
                    badge = "RESULT",
                )
                QuizSurfaceCard {
                    Text(
                        text = stringResource(Res.string.result_section),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    Text(
                        text = stringResource(Res.string.result_correct_count, correctCount, totalCount),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
                    Text(
                        text = stringResource(Res.string.result_score_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$displayedScore",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                QuizPrimaryButton(
                    text = actionLabel,
                    onClick = onGoToRankingClick,
                )
            }
        }
    }
}

@Composable
private fun ConfettiBackground(modifier: Modifier = Modifier) {
    val colors = listOf(
        QuizTokens.primary,
        QuizTokens.correct,
        QuizTokens.secondary,
        QuizTokens.highlight,
    )
    Canvas(modifier = modifier) {
        repeat(32) { i ->
            val x = Random(i).nextFloat() * size.width
            val y = Random(i + 7).nextFloat() * size.height
            drawCircle(
                color = colors[i % colors.size],
                radius = 6f + (i % 3) * 2f,
                center = Offset(x, y),
            )
        }
    }
}
