package com.droidkaigi.quiz.core.ui.preview

import androidx.compose.runtime.Composable
import com.droidkaigi.quiz.core.ui.theme.QuizTheme

@Composable
fun QuizPreview(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit,
) {
    QuizTheme(darkTheme = darkTheme) {
        content()
    }
}
