package com.droidkaigi.quiz.feature.quiz.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .safeContentPadding()
            .padding(QuizTokens.spacingLarge),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = "DroidKaigi 2026 Quiz",
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = "ニックネームを入力してクイズを開始",
            modifier = Modifier.padding(vertical = QuizTokens.spacingMedium),
            style = MaterialTheme.typography.bodyMedium,
        )
        OutlinedTextField(
            value = state.nickname,
            onValueChange = { viewModel.onIntent(HomeIntent.NicknameChanged(it)) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("ニックネーム") },
            singleLine = true,
        )
        state.errorMessage?.let { msg ->
            Text(
                text = msg,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = QuizTokens.spacingSmall),
            )
        }
        Button(
            onClick = { viewModel.onIntent(HomeIntent.StartQuiz) },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = QuizTokens.spacingLarge),
            enabled = !state.isLoading,
        ) {
            if (state.isLoading) {
                CircularProgressIndicator()
            } else {
                Text("クイズを始める")
            }
        }
    }
}
