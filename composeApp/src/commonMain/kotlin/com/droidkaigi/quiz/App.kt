package com.droidkaigi.quiz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.di.initQuizAppGraph
import com.droidkaigi.quiz.navigation.QuizNavHost

@Composable
fun App() {
    LaunchedEffect(Unit) {
        initQuizAppGraph()
    }
    QuizTheme {
        QuizNavHost()
    }
}
