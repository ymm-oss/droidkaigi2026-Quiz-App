package jp.co.yumemi.quiz.droidkaigi.di

import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.di.AppScope
import jp.co.yumemi.quiz.droidkaigi.core.data.di.QuizAppGraph
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

@DependencyGraph(AppScope::class)
interface FakeQuizAppGraph : QuizAppGraph

fun createQuizAppGraph(): QuizAppGraph = createGraph<FakeQuizAppGraph>()

fun initQuizAppGraph() {
    if (!AppDependencies.isInitialized) {
        AppDependencies.init(createQuizAppGraph())
    }
}
