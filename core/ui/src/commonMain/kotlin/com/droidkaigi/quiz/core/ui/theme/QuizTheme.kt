package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun QuizTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) QuizColors.dark() else QuizColors.light()
    MaterialTheme(
        colorScheme = colors,
        typography = QuizTypography.material(),
        content = content,
    )
}
