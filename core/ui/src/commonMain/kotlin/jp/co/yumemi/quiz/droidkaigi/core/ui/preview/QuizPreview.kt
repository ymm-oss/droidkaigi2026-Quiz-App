package jp.co.yumemi.quiz.droidkaigi.core.ui.preview

import androidx.compose.runtime.Composable
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme

@Composable
fun QuizPreview(darkTheme: Boolean = false, content: @Composable () -> Unit) {
    QuizTheme(darkTheme = darkTheme) {
        content()
    }
}
