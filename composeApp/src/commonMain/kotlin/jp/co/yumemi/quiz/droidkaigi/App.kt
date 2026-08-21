package jp.co.yumemi.quiz.droidkaigi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.AppLocaleEnvironment
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import jp.co.yumemi.quiz.droidkaigi.di.initQuizAppGraph
import jp.co.yumemi.quiz.droidkaigi.navigation.QuizNavHost

@Composable
fun App() {
    remember { initQuizAppGraph() }
    AppLocaleEnvironment {
        QuizTheme {
            QuizNavHost()
        }
    }
}
