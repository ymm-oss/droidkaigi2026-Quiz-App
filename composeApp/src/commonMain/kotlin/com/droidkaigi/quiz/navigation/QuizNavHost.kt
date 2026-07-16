package com.droidkaigi.quiz.navigation

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.feature.quiz.home.HomeScreen
import com.droidkaigi.quiz.feature.quiz.quiz.QuizScreen
import com.droidkaigi.quiz.feature.quiz.result.ResultScreen
import com.droidkaigi.quiz.feature.ranking.RankingScreen
import com.droidkaigi.quiz.shell.QuizAdaptiveScaffold

@Composable
fun QuizNavHost() {
    val backStack = remember { mutableStateListOf<Route>(Route.Home) }
    var showExitQuizConfirm by remember { mutableStateOf(false) }

    fun navigate(route: Route) {
        backStack.add(route)
    }

    /** Quiz → Result では Quiz を置き換え、戻る操作で回答済み問題に戻れないようにする。 */
    fun navigateToResult() {
        if (backStack.lastOrNull() == Route.Quiz) {
            backStack.removeLastOrNull()
        }
        backStack.add(Route.Result)
    }

    fun popToHome() {
        backStack.clear()
        backStack.add(Route.Home)
    }

    fun requestLeaveQuiz() {
        showExitQuizConfirm = true
    }

    fun abandonQuizAndGoHome() {
        AppDependencies.shared.sessionHolder.currentSession = null
        showExitQuizConfirm = false
        popToHome()
    }

    fun onBack() {
        if (backStack.size <= 1) return
        if (backStack.lastOrNull() == Route.Quiz) {
            requestLeaveQuiz()
            return
        }
        backStack.removeLastOrNull()
    }

    val provider: (Route) -> NavEntry<Route> = { key ->
        when (key) {
            Route.Home -> NavEntry(key) {
                HomeScreen(onStartQuiz = { navigate(Route.Quiz) })
            }

            Route.Quiz -> NavEntry(key) {
                QuizScreen(onFinished = { navigateToResult() })
            }

            Route.Result -> NavEntry(key) {
                ResultScreen(onGoToRanking = { navigate(Route.Ranking) })
            }

            Route.Ranking -> NavEntry(key) {
                RankingScreen(onGoHome = { popToHome() })
            }
        }
    }

    QuizAdaptiveScaffold(
        currentRoute = backStack.lastOrNull() ?: Route.Home,
        onNavigate = { route ->
            when (route) {
                Route.Home -> {
                    if (backStack.lastOrNull() == Route.Quiz) {
                        requestLeaveQuiz()
                    } else {
                        popToHome()
                    }
                }

                else -> {
                    if (backStack.lastOrNull() != route) {
                        navigate(route)
                    }
                }
            }
        },
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = { onBack() },
            entryProvider = provider,
        )
    }

    if (showExitQuizConfirm) {
        AlertDialog(
            onDismissRequest = { showExitQuizConfirm = false },
            title = { Text("クイズを中断しますか？") },
            text = {
                Text("TOP画面に戻り回答状況が保存されませんが良いでしょうか")
            },
            confirmButton = {
                TextButton(onClick = { abandonQuizAndGoHome() }) {
                    Text("戻る")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitQuizConfirm = false }) {
                    Text("キャンセル")
                }
            },
        )
    }
}
