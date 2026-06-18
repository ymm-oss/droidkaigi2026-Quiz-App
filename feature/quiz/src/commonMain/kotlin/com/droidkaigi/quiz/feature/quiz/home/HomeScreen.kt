package com.droidkaigi.quiz.feature.quiz.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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
import com.droidkaigi.quiz.core.ui.components.QuizPrimaryButton
import com.droidkaigi.quiz.core.ui.components.QuizScreenBackground
import com.droidkaigi.quiz.core.ui.components.QuizSurfaceCard
import com.droidkaigi.quiz.core.ui.components.QuizTextField
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun HomeScreen(
    onStartQuiz: () -> Unit,
    viewModel: HomeViewModel = viewModel { HomeViewModel() },
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.NavigateToQuiz -> onStartQuiz()
            }
        }
    }

    HomeContent(
        nickname = state.nickname,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        onNicknameChange = { viewModel.onIntent(HomeIntent.NicknameChanged(it)) },
        onStartClick = { viewModel.onIntent(HomeIntent.StartQuiz) },
    )
}

@Composable
fun HomeContent(
    nickname: String,
    isLoading: Boolean,
    errorMessage: String?,
    onNicknameChange: (String) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuizScreenBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(horizontal = QuizTokens.spacingLarge)
                    .verticalScroll(rememberScrollState())
                    .imePadding(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingExtraLarge),
            ) {
                QuizHeroTitle(
                    title = "DroidKaigi 2026 Quiz",
                    subtitle = "ニックネームを入力してクイズを開始",
                    badge = "会場クイズ",
                )
                QuizSurfaceCard {
                    Text(
                        text = "プレイヤー情報",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    QuizTextField(
                        value = nickname,
                        onValueChange = onNicknameChange,
                        label = "ニックネーム",
                    )
                    errorMessage?.let { msg ->
                        Text(
                            text = msg,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = QuizTokens.spacingSmall),
                        )
                    }
                }
                QuizPrimaryButton(
                    text = "クイズを始める",
                    onClick = onStartClick,
                    loading = isLoading,
                )
            }
        }
    }
}
