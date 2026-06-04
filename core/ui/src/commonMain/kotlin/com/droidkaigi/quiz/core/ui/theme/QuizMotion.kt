package com.droidkaigi.quiz.core.ui.theme

import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntAsState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

object QuizMotion {
    @Composable
    fun animateSelectionScale(selected: Boolean): Float {
        val target = if (selected) 1.03f else 1f
        val scale by animateFloatAsState(targetValue = target, animationSpec = QuizTokens.selectionSpring)
        return scale
    }

    @Composable
    fun animateScore(target: Int): Int {
        val value by animateIntAsState(targetValue = target, animationSpec = QuizTokens.scoreSpring)
        return value
    }
}

fun Modifier.quizShake(offset: Float): Modifier = graphicsLayer { translationX = offset }
