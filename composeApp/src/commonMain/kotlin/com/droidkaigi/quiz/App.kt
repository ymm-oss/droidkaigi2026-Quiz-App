package com.droidkaigi.quiz

import androidx.compose.runtime.Composable
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.navigation.QuizNavHost

@Composable
fun App() {
    QuizTheme {
        QuizNavHost()
    }
}
