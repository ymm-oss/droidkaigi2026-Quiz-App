package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object QuizColors {
    fun light() = lightColorScheme(
        primary = QuizTokens.primary,
        onPrimary = QuizTokens.onPrimary,
        secondary = QuizTokens.secondary,
        surface = QuizTokens.surface,
        onSurface = QuizTokens.onSurface,
    )

    fun dark() = darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        secondary = Color(0xFFCCC2DC),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
    )
}
