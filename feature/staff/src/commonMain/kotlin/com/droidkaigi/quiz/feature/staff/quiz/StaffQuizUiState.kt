package com.droidkaigi.quiz.feature.staff.quiz

import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.QuizSet

data class StaffQuizUiState(
    val quizSet: QuizSet? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val editorDraft: StaffQuestionDraft? = null,
    val isNewQuestion: Boolean = false,
)

sealed interface StaffQuizIntent {
    data object Refresh : StaffQuizIntent
    data object AddQuestion : StaffQuizIntent
    data class EditQuestion(val question: Question) : StaffQuizIntent
    data class DeleteQuestion(val questionId: String) : StaffQuizIntent
    data object DismissEditor : StaffQuizIntent
    data class UpdateEditorDraft(val draft: StaffQuestionDraft) : StaffQuizIntent
    data object SaveEditor : StaffQuizIntent
    data class ReorderQuestions(val fromIndex: Int, val toIndex: Int) : StaffQuizIntent
}
