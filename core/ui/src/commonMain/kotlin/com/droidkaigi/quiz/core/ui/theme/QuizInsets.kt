package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp

/** Horizontal safe area (cutout / landscape). Apply outside scroll containers. */
@Composable
fun Modifier.quizSafeHorizontalPadding(): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Horizontal))

/**
 * Vertical safe area plus extra padding. Apply **inside** verticalScroll / LazyColumn
 * so content can scroll under system bars, but rests with breathing room at the edges.
 */
@Composable
fun Modifier.quizSafeVerticalPadding(extra: Dp = QuizTokens.spacingLarge): Modifier =
    windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
        .padding(vertical = extra)
