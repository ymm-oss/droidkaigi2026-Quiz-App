package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.luminance

@Composable
fun QuizTheme(darkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colors = if (darkTheme) QuizColors.dark() else QuizColors.light()
    MaterialTheme(
        colorScheme = colors,
        typography = QuizTypography.material(quizFontFamily()),
        content = content,
    )
}

/**
 * Reads darkness from the active scheme instead of the system, so components stay correct
 * under themes that pin a mode (e.g. the dark-only staff console).
 */
@Composable
@ReadOnlyComposable
fun isQuizDarkTheme(): Boolean = MaterialTheme.colorScheme.surface.luminance() < 0.5f
