package com.droidkaigi.quiz.feature.quiz.quiz

import com.droidkaigi.quiz.core.domain.model.Question

data class QuizUiState(
    val prompt: String = "",
    val progress: String = "",
    val progressFraction: Float = 0f,
    val question: Question? = null,
    val selectedSingleId: String? = null,
    val selectedMultipleIds: Set<String> = emptySet(),
    val reorderIds: List<String> = emptyList(),
    val canSubmit: Boolean = false,
    val showFeedback: Boolean = false,
    val lastAnswerCorrect: Boolean? = null,
    val showExitConfirm: Boolean = false,
    /** 最終問回答後〜Result 遷移前。中断ダイアログ・離脱操作を受け付けない */
    val isFinishing: Boolean = false,
)

sealed interface QuizIntent {
    data class SelectSingle(val id: String) : QuizIntent
    data class ToggleMultiple(val id: String) : QuizIntent
    data class MoveReorder(val fromIndex: Int, val toIndex: Int) : QuizIntent
    data object SubmitAnswer : QuizIntent
    data object RequestExit : QuizIntent
    data object ConfirmExit : QuizIntent
    data object DismissExit : QuizIntent
}

sealed interface QuizEvent {
    data object NavigateToResult : QuizEvent
    data object NavigateHome : QuizEvent
}
