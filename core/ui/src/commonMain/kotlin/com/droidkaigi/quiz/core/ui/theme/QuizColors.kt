package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme

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
        primary = QuizTokens.primaryDark,
        onPrimary = QuizTokens.onPrimaryDark,
        primaryContainer = QuizTokens.primaryContainerDark,
        onPrimaryContainer = QuizTokens.onPrimaryContainerDark,
        secondary = QuizTokens.secondaryDark,
        onSecondary = QuizTokens.onSecondaryDark,
        secondaryContainer = QuizTokens.secondaryContainerDark,
        onSecondaryContainer = QuizTokens.onSecondaryContainerDark,
        surface = QuizTokens.surfaceDark,
        onSurface = QuizTokens.onSurfaceDark,
        onSurfaceVariant = QuizTokens.onSurfaceVariantDark,
        outline = QuizTokens.outlineDark,
        error = QuizTokens.incorrect,
    )
}
