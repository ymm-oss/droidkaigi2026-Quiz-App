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
import androidx.compose.foundation.layout.safeContentPadding
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
import com.droidkaigi.quiz.core.ui.theme.QuizMotion
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import kotlin.random.Random

@Composable
fun ResultScreen(
    onGoToRanking: () -> Unit,
    viewModel: ResultViewModel = viewModel { ResultViewModel() },
) {
    val state by viewModel.uiState.collectAsState()
    val animatedScore = QuizMotion.animateScore(state.targetScore)

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                ResultEvent.NavigateToRanking -> onGoToRanking()
            }
        }
    }

    QuizScreenBackground {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
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
                    .padding(horizontal = QuizTokens.spacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingExtraLarge),
            ) {
                QuizHeroTitle(
                    title = "クイズ完了",
                    subtitle = "おつかれさま、${state.nickname} さん！",
                    badge = "RESULT",
                )
                QuizSurfaceCard {
                    Text(
                        text = "あなたの結果",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    Text(
                        text = "${state.correctCount} / ${state.totalCount} 問正解",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
                    Text(
                        text = "スコア",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Text(
                        text = "$animatedScore",
                        style = MaterialTheme.typography.displaySmall,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                QuizPrimaryButton(
                    text = "ランキングを見る",
                    onClick = { viewModel.onIntent(ResultIntent.GoToRanking) },
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
