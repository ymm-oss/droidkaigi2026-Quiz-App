package com.droidkaigi.quiz.core.domain.model

sealed interface Answer {
    val questionId: String
}

data class SingleChoiceAnswer(override val questionId: String, val selectedId: String) : Answer

data class MultipleChoiceAnswer(override val questionId: String, val selectedIds: Set<String>) : Answer

data class ReorderAnswer(override val questionId: String, val orderedIds: List<String>) : Answer
