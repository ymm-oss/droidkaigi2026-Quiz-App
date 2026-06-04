package com.droidkaigi.quiz.core.domain.model

import kotlinx.serialization.Serializable

/** Groups a quiz set and its rankings (e.g. by day or difficulty). */
@Serializable
data class QuizFolder(
    val id: String,
    val name: String,
    val description: String = "",
    val sortOrder: Int = 0,
)
