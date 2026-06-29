package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun QuizScreenBackground(modifier: Modifier = Modifier, content: @Composable () -> Unit) {
    val darkTheme = isSystemInDarkTheme()
    val gradientStart = if (darkTheme) QuizTokens.gradientStartDark else QuizTokens.gradientStartLight
    val gradientEnd = if (darkTheme) QuizTokens.gradientEndDark else QuizTokens.gradientEndLight
    val orbColor = if (darkTheme) QuizTokens.gradientOrbDark else QuizTokens.gradientOrbLight

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(gradientStart, gradientEnd),
                ),
            ),
    ) {
        Box(
            modifier = Modifier
                .size(220.dp)
                .offset(x = (-48).dp, y = (-32).dp)
                .clip(CircleShape)
                .background(orbColor),
        )
        Box(
            modifier = Modifier
                .size(160.dp)
                .offset(x = 280.dp, y = 120.dp)
                .clip(CircleShape)
                .background(orbColor),
        )
        content()
    }
}
