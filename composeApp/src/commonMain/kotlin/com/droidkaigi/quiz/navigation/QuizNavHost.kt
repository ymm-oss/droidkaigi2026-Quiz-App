package com.droidkaigi.quiz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.ui.NavDisplay
import com.droidkaigi.quiz.feature.quiz.home.HomeScreen
import com.droidkaigi.quiz.feature.quiz.quiz.QuizScreen
import com.droidkaigi.quiz.feature.quiz.result.ResultScreen
import com.droidkaigi.quiz.feature.ranking.RankingScreen
import com.droidkaigi.quiz.shell.QuizAdaptiveScaffold
import kotlinx.coroutines.flow.MutableSharedFlow

@Composable
fun QuizNavHost() {
    val backStack = remember { mutableStateListOf<Route>(Route.Home) }
    val leaveQuizRequest = remember {
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    }
    var quizExitEnabled by remember { mutableStateOf(true) }

    fun navigate(route: Route) {
        backStack.add(route)
    }

    /** Quiz → Result では Quiz を置き換え、戻る操作で回答済み問題に戻れないようにする。 */
    fun navigateToResult() {
        quizExitEnabled = true
        if (backStack.lastOrNull() == Route.Quiz) {
            backStack.removeLastOrNull()
        }
        backStack.add(Route.Result)
    }

    fun popToHome() {
        quizExitEnabled = true
        backStack.clear()
        backStack.add(Route.Home)
    }

    fun requestLeaveQuiz() {
        if (!quizExitEnabled) return
        leaveQuizRequest.tryEmit(Unit)
    }

    fun onBack() {
        if (backStack.size <= 1) return
        if (backStack.lastOrNull() == Route.Quiz) {
            // 完走中は離脱不可。回答中は中断確認のみ（スタックは pop しない）
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
                QuizScreen(
                    onFinished = { navigateToResult() },
                    onAbandoned = { popToHome() },
                    leaveRequest = leaveQuizRequest,
                    onExitEnabledChange = { quizExitEnabled = it },
                )
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

                Route.Ranking -> {
                    if (backStack.lastOrNull() == Route.Quiz) {
                        // Quiz 中の Ranking は中断確認なしで積めると進捗破棄をバイパスするため、Home と同様に確認する
                        requestLeaveQuiz()
                    } else if (backStack.lastOrNull() != Route.Ranking) {
                        navigate(Route.Ranking)
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
}
