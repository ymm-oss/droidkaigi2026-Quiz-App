package com.droidkaigi.quiz.core.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState

@Composable
fun QuizMarkdownText(markdown: String, modifier: Modifier = Modifier) {
    if (markdown.isBlank()) return
    val state = rememberMarkdownState(markdown, retainState = true)
    Markdown(
        state,
        modifier = modifier,
    )
}
