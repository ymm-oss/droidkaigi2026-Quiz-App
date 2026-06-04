package com.droidkaigi.quiz.feature.quiz.result

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(QuizTokens.spacingLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ConfettiBackground(modifier = Modifier.fillMaxWidth().weight(1f))
        Text(text = "おつかれさま、${state.nickname} さん！", style = MaterialTheme.typography.headlineSmall)
        Text(
            text = "${state.correctCount} / ${state.totalCount} 問正解",
            modifier = Modifier.padding(vertical = QuizTokens.spacingMedium),
        )
        Text(
            text = "スコア: $animatedScore",
            style = MaterialTheme.typography.displaySmall,
            color = MaterialTheme.colorScheme.primary,
        )
        Button(
            onClick = { viewModel.onIntent(ResultIntent.GoToRanking) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = QuizTokens.spacingLarge),
        ) {
            Text("ランキングを見る")
        }
    }
}

@Composable
private fun ConfettiBackground(modifier: Modifier = Modifier) {
    val colors = listOf(QuizTokens.primary, QuizTokens.correct, QuizTokens.secondary, QuizTokens.highlight)
    Canvas(modifier = modifier) {
        repeat(24) { i ->
            val x = Random(i).nextFloat() * size.width
            val y = Random(i + 7).nextFloat() * size.height * 0.5f
            drawCircle(colors[i % colors.size], radius = 6f + (i % 3) * 2f, center = Offset(x, y))
        }
    }
}
