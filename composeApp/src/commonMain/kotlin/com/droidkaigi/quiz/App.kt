package com.droidkaigi.quiz

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.droidkaigi.quiz.core.ui.locale.AppLocaleEnvironment
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.di.initQuizAppGraph
import com.droidkaigi.quiz.navigation.QuizNavHost

@Composable
fun App() {
    remember { initQuizAppGraph() }
    AppLocaleEnvironment {
        QuizTheme {
            QuizNavHost()
        }
    }
}
