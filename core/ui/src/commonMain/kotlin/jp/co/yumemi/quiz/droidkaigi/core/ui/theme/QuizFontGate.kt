package jp.co.yumemi.quiz.droidkaigi.core.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.delay

/** Keep a font that loads quickly from flashing a spinner over the UI. */
private const val PROGRESS_DELAY_MILLIS = 300L

/**
 * Holds [content] back until the theme font can render, so text is never painted with a fallback
 * font first. Only the Wasm build waits — other targets are ready on the first composition.
 */
@Composable
internal fun QuizFontGate(isReady: Boolean, content: @Composable () -> Unit) {
    if (isReady) {
        content()
        return
    }
    var showProgress by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(PROGRESS_DELAY_MILLIS)
        showProgress = true
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center,
    ) {
        if (showProgress) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
    }
}
