package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Edit this file only to retheme the whole app. */
object QuizTokens {
    // Droid-kun / Android green palette (light)
    val primary = Color(0xFF3DDC84)
    val onPrimary = Color(0xFF003822)
    val primaryContainer = Color(0xFFC8FADC)
    val onPrimaryContainer = Color(0xFF0B4624)
    val secondary = Color(0xFF4F6356)
    val onSecondary = Color(0xFFFFFFFF)
    val secondaryContainer = Color(0xFFD3E8D8)
    val onSecondaryContainer = Color(0xFF1B3525)
    val surface = Color(0xFFF7FCF9)
    val onSurface = Color(0xFF191C1A)
    val onSurfaceVariant = Color(0xFF404943)
    val surfaceElevated = Color(0xFFFFFFFF)
    val outline = Color(0xFF707972)
    val correct = Color(0xFF2E7D32)
    val incorrect = Color(0xFFC62828)
    val highlight = Color(0xFFC8FADC)
    val accent = Color(0xFFA4C639)

    val gradientStartLight = Color(0xFFE8F9EF)
    val gradientEndLight = Color(0xFFF7FCF9)
    val gradientOrbLight = Color(0x333DDC84)

    // Dark theme
    val primaryDark = Color(0xFF6FE89F)
    val onPrimaryDark = Color(0xFF003822)
    val primaryContainerDark = Color(0xFF1B5E3A)
    val onPrimaryContainerDark = Color(0xFFB8F5D0)
    val secondaryDark = Color(0xFFB8CCB8)
    val onSecondaryDark = Color(0xFF243429)
    val secondaryContainerDark = Color(0xFF3A4B40)
    val onSecondaryContainerDark = Color(0xFFD3E8D8)
    val surfaceDark = Color(0xFF101814)
    val onSurfaceDark = Color(0xFFE2E8E4)
    val onSurfaceVariantDark = Color(0xFFBFC9C2)
    val outlineDark = Color(0xFF89938C)
    val highlightDark = Color(0xFF1B5E3A)

    val gradientStartDark = Color(0xFF0D1F14)
    val gradientEndDark = Color(0xFF101814)
    val gradientOrbDark = Color(0x336FE89F)

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
