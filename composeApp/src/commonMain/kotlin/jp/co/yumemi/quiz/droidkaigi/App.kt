package jp.co.yumemi.quiz.droidkaigi

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.bindAppConfig
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.AppLocaleEnvironment
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTheme
import jp.co.yumemi.quiz.droidkaigi.di.initQuizAppGraph
import jp.co.yumemi.quiz.droidkaigi.navigation.QuizNavHost

@Composable
fun App() {
    remember { initQuizAppGraph() }
    BindAppConfig()
    AppLocaleEnvironment {
        QuizTheme {
            QuizNavHost()
        }
    }
}

@Composable
private fun BindAppConfig() {
    val deps = AppDependencies.shared
    val retryToken by deps.siteStatusHolder.retryToken.collectAsState()
    LaunchedEffect(retryToken) {
        deps.siteStatusHolder.bindAppConfig(deps.observeAppConfigUseCase)
    }
}
