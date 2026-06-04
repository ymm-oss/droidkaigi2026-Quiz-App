package com.droidkaigi.quiz.core.domain.repository

import com.droidkaigi.quiz.core.domain.model.QuizSet

interface QuizRepository {
    suspend fun getDefaultQuizSet(): QuizSet
}
