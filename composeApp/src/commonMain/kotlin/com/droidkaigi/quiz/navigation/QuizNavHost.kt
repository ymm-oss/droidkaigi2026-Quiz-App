package com.droidkaigi.quiz.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
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

    QuizAdaptiveScaffold(
        currentRoute = backStack.lastOrNull() ?: Route.Home,
        onNavigate = { route ->
            handleScaffoldNavigate(
                route = route,
                backStack = backStack,
                requestLeaveQuiz = ::requestLeaveQuiz,
                popToHome = ::popToHome,
                navigate = ::navigate,
            )
        },
    ) {
        NavDisplay(
            backStack = backStack,
            onBack = {
                handleBack(
                    backStack = backStack,
                    requestLeaveQuiz = ::requestLeaveQuiz,
                )
            },
            entryProvider = { key ->
                quizNavEntry(
                    key = key,
                    onStartQuiz = { navigate(Route.Quiz) },
                    onQuizFinished = ::navigateToResult,
                    onQuizAbandoned = ::popToHome,
                    leaveRequest = leaveQuizRequest,
                    onExitEnabledChange = { quizExitEnabled = it },
                    onGoToRanking = { navigate(Route.Ranking) },
                    onGoHome = ::popToHome,
                )
            },
        )
    }
}

private fun handleBack(backStack: SnapshotStateList<Route>, requestLeaveQuiz: () -> Unit) {
    if (backStack.size <= 1) return
    if (backStack.lastOrNull() == Route.Quiz) {
        // 完走中は離脱不可。回答中は中断確認のみ（スタックは pop しない）
        requestLeaveQuiz()
        return
    }
    backStack.removeLastOrNull()
}

private fun handleScaffoldNavigate(
    route: Route,
    backStack: SnapshotStateList<Route>,
    requestLeaveQuiz: () -> Unit,
    popToHome: () -> Unit,
    navigate: (Route) -> Unit,
) {
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
}

private fun quizNavEntry(
    key: Route,
    onStartQuiz: () -> Unit,
    onQuizFinished: () -> Unit,
    onQuizAbandoned: () -> Unit,
    leaveRequest: MutableSharedFlow<Unit>,
    onExitEnabledChange: (Boolean) -> Unit,
    onGoToRanking: () -> Unit,
    onGoHome: () -> Unit,
): NavEntry<Route> = when (key) {
    Route.Home -> NavEntry(key) {
        HomeScreen(onStartQuiz = onStartQuiz)
    }

    Route.Quiz -> NavEntry(key) {
        QuizScreen(
            onFinished = onQuizFinished,
            onAbandoned = onQuizAbandoned,
            leaveRequest = leaveRequest,
            onExitEnabledChange = onExitEnabledChange,
        )
    }

    Route.Result -> NavEntry(key) {
        ResultScreen(onGoToRanking = onGoToRanking)
    }

    Route.Ranking -> NavEntry(key) {
        RankingScreen(onGoHome = onGoHome)
    }
}
