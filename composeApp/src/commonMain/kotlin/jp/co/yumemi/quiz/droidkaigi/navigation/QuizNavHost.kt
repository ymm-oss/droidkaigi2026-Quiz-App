package jp.co.yumemi.quiz.droidkaigi.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavEntry
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import androidx.savedstate.serialization.SavedStateConfiguration
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.SiteStatusHolder
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.home.HomeScreen
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz.QuizScreen
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.result.ResultScreen
import jp.co.yumemi.quiz.droidkaigi.feature.ranking.RankingScreen
import jp.co.yumemi.quiz.droidkaigi.shell.QuizAdaptiveScaffold
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic

/** 保存・復元時に NavKey の open polymorphism から Route の各サブタイプを解決する。 */
private val quizNavStateConfiguration = SavedStateConfiguration {
    serializersModule = SerializersModule {
        polymorphic(NavKey::class) {
            subclass(Route.Home::class, Route.Home.serializer())
            subclass(Route.Quiz::class, Route.Quiz.serializer())
            subclass(Route.Result::class, Route.Result.serializer())
            subclass(Route.Ranking::class, Route.Ranking.serializer())
        }
    }
}

@Composable
fun QuizNavHost(siteStatusHolder: SiteStatusHolder = AppDependencies.shared.siteStatusHolder) {
    // rememberNavBackStack keeps the stack across configuration changes (rotation, resize)
    // and process death, so an in-progress quiz is not reset to Home.
    val backStack = rememberNavBackStack(quizNavStateConfiguration, Route.Home)
    val leaveQuizRequest = remember {
        MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    }
    var quizExitEnabled by remember { mutableStateOf(true) }
    val sitePublished by siteStatusHolder.sitePublished.collectAsState()
    val rankingNavVisible = sitePublished == true

    LaunchedEffect(rankingNavVisible, backStack.lastOrNull()) {
        if (!rankingNavVisible && backStack.lastOrNull() == Route.Ranking) {
            backStack.clear()
            backStack.add(Route.Home)
        }
    }

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
        currentRoute = backStack.lastOrNull() as? Route ?: Route.Home,
        rankingNavVisible = rankingNavVisible,
        onNavigate = { route ->
            handleScaffoldNavigate(
                route = route,
                rankingNavVisible = rankingNavVisible,
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
                    rankingNavVisible = rankingNavVisible,
                    onStartQuiz = { navigate(Route.Quiz) },
                    onQuizFinished = ::navigateToResult,
                    onQuizAbandoned = ::popToHome,
                    leaveRequest = leaveQuizRequest,
                    onExitEnabledChange = { quizExitEnabled = it },
                    onGoToRanking = {
                        if (rankingNavVisible) {
                            navigate(Route.Ranking)
                        }
                    },
                    onGoHome = ::popToHome,
                )
            },
        )
    }
}

private fun handleBack(backStack: NavBackStack<NavKey>, requestLeaveQuiz: () -> Unit) {
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
    rankingNavVisible: Boolean,
    backStack: NavBackStack<NavKey>,
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
            if (!rankingNavVisible) return
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
    key: NavKey,
    rankingNavVisible: Boolean,
    onStartQuiz: () -> Unit,
    onQuizFinished: () -> Unit,
    onQuizAbandoned: () -> Unit,
    leaveRequest: MutableSharedFlow<Unit>,
    onExitEnabledChange: (Boolean) -> Unit,
    onGoToRanking: () -> Unit,
    onGoHome: () -> Unit,
): NavEntry<NavKey> = when (key) {
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
        ResultScreen(
            rankingVisible = rankingNavVisible,
            onGoToRanking = onGoToRanking,
            onMissingResult = onGoHome,
        )
    }

    Route.Ranking -> NavEntry(key) {
        RankingScreen(onGoHome = onGoHome)
    }

    else -> error("Unknown route: $key")
}
