package com.droidkaigi.quiz.di

import com.droidkaigi.quiz.core.data.di.QuizAppGraph

/** Built in the active runtime source set (`fakeMain` / `prodMain`). */
expect fun createQuizAppGraph(): QuizAppGraph

expect fun initQuizAppGraph()
