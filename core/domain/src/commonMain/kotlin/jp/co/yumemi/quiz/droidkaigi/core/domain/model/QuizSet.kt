package jp.co.yumemi.quiz.droidkaigi.core.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class QuizSet(val id: String, val title: String, val questions: List<Question>)
