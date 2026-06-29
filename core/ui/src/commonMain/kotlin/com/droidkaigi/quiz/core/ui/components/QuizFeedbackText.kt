package com.droidkaigi.quiz.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun QuizFeedbackText(isCorrect: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = if (isCorrect) "正解！" else "不正解",
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = if (isCorrect) QuizTokens.correct else QuizTokens.incorrect,
    )
}
