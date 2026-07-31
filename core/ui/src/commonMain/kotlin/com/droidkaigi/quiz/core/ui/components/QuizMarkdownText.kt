package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.mikepenz.markdown.compose.LocalMarkdownColors
import com.mikepenz.markdown.compose.LocalMarkdownDimens
import com.mikepenz.markdown.compose.LocalMarkdownPadding
import com.mikepenz.markdown.compose.components.markdownComponents
import com.mikepenz.markdown.compose.elements.MarkdownCodeBlock
import com.mikepenz.markdown.compose.elements.MarkdownCodeFence
import com.mikepenz.markdown.m3.Markdown
import com.mikepenz.markdown.model.rememberMarkdownState

@Composable
fun QuizMarkdownText(markdown: String, modifier: Modifier = Modifier) {
    if (markdown.isBlank()) return
    val state = rememberMarkdownState(markdown, retainState = true)
    Markdown(
        state,
        modifier = modifier,
        components = markdownComponents(
            codeFence = { model ->
                MarkdownCodeFence(
                    content = model.content,
                    node = model.node,
                    style = model.typography.code,
                ) { code, _, style ->
                    QuizScrollableCodeBlock(code = code, style = style)
                }
            },
            codeBlock = { model ->
                MarkdownCodeBlock(
                    content = model.content,
                    node = model.node,
                    style = model.typography.code,
                ) { code, _, style ->
                    QuizScrollableCodeBlock(code = code, style = style)
                }
            },
        ),
    )
}

/**
 * Fenced / indented code: no soft wrap, horizontal scroll, full text for TalkBack.
 * Avoids the library default [pointerInput] that can swallow scroll gestures.
 */
@Composable
private fun QuizScrollableCodeBlock(
    code: String,
    style: TextStyle,
) {
    val colors = LocalMarkdownColors.current
    val dimens = LocalMarkdownDimens.current
    val padding = LocalMarkdownPadding.current
    val scrollState = rememberScrollState()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clip(RoundedCornerShape(dimens.codeBackgroundCornerSize))
            .background(colors.codeBackground)
            .semantics {
                contentDescription = code
            },
    ) {
        Text(
            text = code,
            style = style,
            softWrap = false,
            modifier = Modifier
                .horizontalScroll(scrollState)
                .padding(padding.codeBlock)
                .clearAndSetSemantics { },
        )
    }
}
