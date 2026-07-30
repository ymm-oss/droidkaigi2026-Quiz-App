package com.droidkaigi.quiz.core.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.droidkaigi.quiz.core.ui.generated.resources.Res
import com.droidkaigi.quiz.core.ui.generated.resources.feedback_correct
import com.droidkaigi.quiz.core.ui.generated.resources.feedback_incorrect
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import org.jetbrains.compose.resources.stringResource

@Composable
fun QuizFeedbackText(isCorrect: Boolean, modifier: Modifier = Modifier) {
    Text(
        text = if (isCorrect) {
            stringResource(Res.string.feedback_correct)
        } else {
            stringResource(Res.string.feedback_incorrect)
        },
        modifier = modifier,
        style = MaterialTheme.typography.titleMedium,
        color = if (isCorrect) QuizTokens.correct else QuizTokens.incorrect,
    )
}
