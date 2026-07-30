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

    // Staff console ("Android Green Operations") — dark-only, tonal layering for venue operation.
    val staffPrimary = Color(0xFFAEFFC7)
    val staffOnPrimary = Color(0xFF00391E)
    val staffPrimaryContainer = Color(0xFF6FE89F)
    val staffOnPrimaryContainer = Color(0xFF00673A)
    val staffSecondary = Color(0xFFB8CBBD)
    val staffOnSecondary = Color(0xFF24342A)
    val staffSecondaryContainer = Color(0xFF3A4B40)
    val staffOnSecondaryContainer = Color(0xFFD4E7D8)
    val staffTertiary = Color(0xFFB8FDCD)
    val staffOnTertiary = Color(0xFF00391E)
    val staffTertiaryContainer = Color(0xFF9DE0B2)
    val staffOnTertiaryContainer = Color(0xFF236540)
    val staffSurface = Color(0xFF0D1511)
    val staffOnSurface = Color(0xFFE2E8E4)
    val staffSurfaceVariant = Color(0xFF2E3732)
    val staffOnSurfaceVariant = Color(0xFFBFC9C2)
    val staffSurfaceContainerLowest = Color(0xFF08100C)
    val staffSurfaceContainerLow = Color(0xFF151D19)
    val staffSurfaceContainer = Color(0xFF19211D)
    val staffSurfaceContainerHigh = Color(0xFF242C27)
    val staffSurfaceContainerHighest = Color(0xFF2E3732)
    val staffSurfaceBright = Color(0xFF333B36)
    val staffInverseSurface = Color(0xFFDCE5DE)
    val staffInverseOnSurface = Color(0xFF2A322E)
    val staffInversePrimary = Color(0xFF006D3E)
    val staffOutline = Color(0xFF89938C)
    val staffOutlineVariant = Color(0xFF3D4A40)
    val staffError = Color(0xFFFFB4AB)
    val staffOnError = Color(0xFF690005)
    val staffErrorContainer = Color(0xFF93000A)
    val staffOnErrorContainer = Color(0xFFFFDAD6)

    // Staff console layout metrics (1440x900 desktop reference).
    val staffNavRailWidth = 80.dp
    val staffSidebarWidth = 260.dp
    val staffEditorPanelWidth = 450.dp
    val staffContentMaxWidth = 1000.dp
    val staffTopBarHeight = 64.dp
    val staffTableRowHeight = 56.dp

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
