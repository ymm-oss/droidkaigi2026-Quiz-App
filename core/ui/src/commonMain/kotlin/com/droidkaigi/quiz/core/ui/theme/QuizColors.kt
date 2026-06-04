package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

object QuizColors {
    fun light() = lightColorScheme(
        primary = QuizTokens.primary,
        onPrimary = QuizTokens.onPrimary,
        primaryContainer = QuizTokens.primaryContainer,
        onPrimaryContainer = QuizTokens.onPrimaryContainer,
        secondary = QuizTokens.secondary,
        onSecondary = QuizTokens.onSecondary,
        secondaryContainer = QuizTokens.secondaryContainer,
        onSecondaryContainer = QuizTokens.onSecondaryContainer,
        surface = QuizTokens.surface,
        onSurface = QuizTokens.onSurface,
        onSurfaceVariant = QuizTokens.onSurfaceVariant,
        outline = QuizTokens.outline,
        error = QuizTokens.incorrect,
    )

    fun dark() = darkColorScheme(
        primary = Color(0xFFD0BCFF),
        onPrimary = Color(0xFF381E72),
        primaryContainer = Color(0xFF4F378B),
        onPrimaryContainer = Color(0xFFEADDFF),
        secondary = Color(0xFFCCC2DC),
        onSecondary = Color(0xFF332D41),
        secondaryContainer = Color(0xFF4A4458),
        onSecondaryContainer = Color(0xFFE8DEF8),
        surface = Color(0xFF1C1B1F),
        onSurface = Color(0xFFE6E1E5),
        onSurfaceVariant = Color(0xFFCAC4D0),
        outline = Color(0xFF938F99),
        error = Color(0xFFF2B8B5),
    )
}
