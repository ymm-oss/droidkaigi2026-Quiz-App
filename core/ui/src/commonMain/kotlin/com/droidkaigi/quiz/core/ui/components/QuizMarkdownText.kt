package com.droidkaigi.quiz.core.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

@Composable
fun QuizMarkdownText(
    markdown: String,
    modifier: Modifier = Modifier,
) {
    if (markdown.isBlank()) return
    val blocks = remember(markdown) { parseMarkdownBlocks(markdown) }
    Column(modifier = modifier) {
        blocks.forEach { block ->
            when (block) {
                is MarkdownBlock.Spacer -> Spacer(modifier = Modifier.height(QuizTokens.spacingSmall))
                is MarkdownBlock.Heading -> Text(
                    text = block.text,
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                is MarkdownBlock.Bullet -> Text(
                    text = "• ${block.text}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                is MarkdownBlock.Paragraph -> Text(
                    text = block.annotated,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

private sealed interface MarkdownBlock {
    data object Spacer : MarkdownBlock
    data class Heading(val text: String) : MarkdownBlock
    data class Bullet(val text: String) : MarkdownBlock
    data class Paragraph(val annotated: AnnotatedString) : MarkdownBlock
}

private fun parseMarkdownBlocks(markdown: String): List<MarkdownBlock> {
    val blocks = mutableListOf<MarkdownBlock>()
    val lines = markdown.replace("\r\n", "\n").lines()
    var index = 0
    while (index < lines.size) {
        val line = lines[index]
        when {
            line.isBlank() -> blocks += MarkdownBlock.Spacer
            line.startsWith("## ") -> blocks += MarkdownBlock.Heading(line.removePrefix("## ").trim())
            line.startsWith("# ") -> blocks += MarkdownBlock.Heading(line.removePrefix("# ").trim())
            line.startsWith("- ") -> blocks += MarkdownBlock.Bullet(line.removePrefix("- ").trim())
            else -> blocks += MarkdownBlock.Paragraph(inlineMarkdown(line.trim()))
        }
        index++
    }
    return blocks
}

private fun inlineMarkdown(text: String): AnnotatedString = buildAnnotatedString {
    var cursor = 0
    while (cursor < text.length) {
        val boldStart = text.indexOf("**", cursor)
        val codeStart = text.indexOf('`', cursor)
        val next = listOf(
            boldStart.takeIf { it >= 0 },
            codeStart.takeIf { it >= 0 },
        ).filterNotNull().minOrNull() ?: -1

        if (next < 0) {
            append(text.substring(cursor))
            break
        }

        if (next > cursor) append(text.substring(cursor, next))

        when (next) {
            boldStart -> {
                val end = text.indexOf("**", next + 2)
                if (end < 0) {
                    append(text.substring(next))
                    break
                }
                val contentStart = pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                append(text.substring(next + 2, end))
                pop(contentStart)
                cursor = end + 2
            }
            codeStart -> {
                val end = text.indexOf('`', codeStart + 1)
                if (end < 0) {
                    append(text.substring(codeStart))
                    break
                }
                val contentStart = pushStyle(SpanStyle(fontFamily = FontFamily.Monospace))
                append(text.substring(codeStart + 1, end))
                pop(contentStart)
                cursor = end + 1
            }
            else -> {
                append(text.substring(next))
                cursor = text.length
            }
        }
    }
}
