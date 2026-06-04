package com.droidkaigi.quiz.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizSet(
    val id: String,
    val title: String,
    val questions: List<Question>,
)
