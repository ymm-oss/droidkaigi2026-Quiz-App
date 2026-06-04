package com.droidkaigi.quiz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import com.droidkaigi.quiz.feature.quiz.home.HomeScreen
import com.droidkaigi.quiz.feature.quiz.quiz.QuizScreen
import com.droidkaigi.quiz.feature.quiz.result.ResultScreen
import com.droidkaigi.quiz.feature.ranking.RankingScreen
import com.droidkaigi.quiz.shell.QuizAdaptiveScaffold

@Composable
fun QuizNavHost() {
    val backStack = remember { mutableStateListOf<Route>(Route.Home) }

    fun navigate(route: Route) {
        backStack.add(route)
    }

    fun popToHome() {
        backStack.clear()
        backStack.add(Route.Home)
    }

    val provider = entryProvider {
        entry<Route.Home> {
            HomeScreen(onStartQuiz = { navigate(Route.Quiz) })
        }
        entry<Route.Quiz> {
            QuizScreen(onFinished = { navigate(Route.Result) })
        }
        entry<Route.Result> {
            ResultScreen(onGoToRanking = { navigate(Route.Ranking) })
        }
        entry<Route.Ranking> {
            RankingScreen(onGoHome = { popToHome() })
        }
    }

    QuizAdaptiveScaffold(
        currentRoute = backStack.lastOrNull() ?: Route.Home,
        onNavigate = { route ->
            when (route) {
                Route.Home -> popToHome()
                else -> {
                    if (backStack.lastOrNull() != route) {
                        if (route == Route.Home) popToHome() else navigate(route)
                    }
                }
            }
        },
    ) {
        NavDisplay(
            backStack = backStack,
            entryProvider = provider,
        )
    }
}
