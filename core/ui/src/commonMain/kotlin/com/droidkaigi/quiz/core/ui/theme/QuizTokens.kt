package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/** Edit this file only to retheme the whole app. */
object QuizTokens {
    val primary = Color(0xFF6750A4)
    val onPrimary = Color(0xFFFFFFFF)
    val secondary = Color(0xFF625B71)
    val surface = Color(0xFFFFFBFE)
    val onSurface = Color(0xFF1C1B1F)
    val correct = Color(0xFF2E7D32)
    val incorrect = Color(0xFFC62828)
    val highlight = Color(0xFFE8DEF8)

    val cornerLarge = 16.dp
    val cornerMedium = 12.dp
    val spacingSmall = 8.dp
    val spacingMedium = 16.dp
    val spacingLarge = 24.dp

    val selectionSpring = spring<Float>(dampingRatio = Spring.DampingRatioMediumBouncy)
    val scoreSpring = spring<Int>(dampingRatio = Spring.DampingRatioLowBouncy)
}
