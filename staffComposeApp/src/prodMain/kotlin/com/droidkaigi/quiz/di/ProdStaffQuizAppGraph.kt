package com.droidkaigi.quiz.di

import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.data.di.AppScope
import com.droidkaigi.quiz.core.data.di.QuizAppGraph
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.createGraph

@DependencyGraph(AppScope::class)
interface ProdStaffQuizAppGraph : QuizAppGraph

fun createStaffQuizAppGraph(): QuizAppGraph = createGraph<ProdStaffQuizAppGraph>()

fun initStaffQuizAppGraph() {
    if (!AppDependencies.isInitialized) {
        AppDependencies.init(createStaffQuizAppGraph())
    }
}
