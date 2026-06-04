package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Edit this file only to retheme the whole app. */
object QuizTokens {
    val primary = Color(0xFF6750A4)
    val onPrimary = Color(0xFFFFFFFF)
    val primaryContainer = Color(0xFFEADDFF)
    val onPrimaryContainer = Color(0xFF21005D)
    val secondary = Color(0xFF625B71)
    val onSecondary = Color(0xFFFFFFFF)
    val secondaryContainer = Color(0xFFE8DEF8)
    val onSecondaryContainer = Color(0xFF1D192B)
    val surface = Color(0xFFFFFBFE)
    val onSurface = Color(0xFF1C1B1F)
    val onSurfaceVariant = Color(0xFF49454F)
    val surfaceElevated = Color(0xFFFFFFFF)
    val outline = Color(0xFF79747E)
    val correct = Color(0xFF2E7D32)
    val incorrect = Color(0xFFC62828)
    val highlight = Color(0xFFE8DEF8)
    val accent = Color(0xFF7F67BE)

    val gradientStartLight = Color(0xFFF3EEFF)
    val gradientEndLight = Color(0xFFFFFBFE)
    val gradientOrbLight = Color(0x337F67BE)

    val gradientStartDark = Color(0xFF1C1528)
    val gradientEndDark = Color(0xFF1C1B1F)
    val gradientOrbDark = Color(0x33D0BCFF)

    val cornerSmall = 8.dp
    val cornerMedium = 12.dp
    val cornerLarge = 16.dp
    val cornerExtraLarge = 24.dp

    val spacingSmall = 8.dp
    val spacingMedium = 16.dp
    val spacingLarge = 24.dp
    val spacingExtraLarge = 32.dp

    val selectionSpring = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy)
    val scoreSpring = spring<Int>(dampingRatio = Spring.DampingRatioLowBouncy)
}
