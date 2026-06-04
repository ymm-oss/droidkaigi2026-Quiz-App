package com.droidkaigi.quiz.feature.quiz.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.MultipleChoiceAnswer
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.ReorderAnswer
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.domain.model.SingleChoiceAnswer
import com.droidkaigi.quiz.core.domain.scoring.QuizScorer
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizViewModel(
    private val deps: AppDependencies = AppDependencies.shared,
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuizEvent>()
    val events: SharedFlow<QuizEvent> = _events.asSharedFlow()

    init {
        refreshFromSession()
    }

    private fun session() = deps.sessionHolder.currentSession

    private fun refreshFromSession() {
        val session = session() ?: return
        val question = session.currentQuestion
        _uiState.update {
            QuizUiState(
                prompt = question?.prompt.orEmpty(),
                progress = session.progressLabel,
                question = question,
                selectedSingleId = null,
                selectedMultipleIds = emptySet(),
                reorderIds = (question as? Reorder)?.items?.map { it.id }.orEmpty(),
                canSubmit = false,
                showFeedback = false,
                lastAnswerCorrect = null,
            )
        }
    }

    fun onIntent(intent: QuizIntent) {
        when (intent) {
            is QuizIntent.SelectSingle -> _uiState.update {
                it.copy(selectedSingleId = intent.id, canSubmit = true)
            }
            is QuizIntent.ToggleMultiple -> _uiState.update {
                val next = if (intent.id in it.selectedMultipleIds) {
                    it.selectedMultipleIds - intent.id
                } else {
                    it.selectedMultipleIds + intent.id
                }
                it.copy(selectedMultipleIds = next, canSubmit = next.isNotEmpty())
            }
            is QuizIntent.MoveReorder -> {
                val ids = _uiState.value.reorderIds.toMutableList()
                if (intent.fromIndex in ids.indices && intent.toIndex in ids.indices) {
                    val item = ids.removeAt(intent.fromIndex)
                    ids.add(intent.toIndex, item)
                    _uiState.update { it.copy(reorderIds = ids, canSubmit = true) }
                }
            }
            QuizIntent.SubmitAnswer -> submitAnswer()
        }
    }

    private fun submitAnswer() {
        val session = session() ?: return
        val question = session.currentQuestion ?: return
        val answer = buildAnswer(question, _uiState.value) ?: return
        val correct = QuizScorer.isCorrect(question, answer)
        var updated = deps.quizEngine.submitAnswer(session, answer)
        updated = deps.quizEngine.advance(updated)
        deps.sessionHolder.currentSession = updated

        _uiState.update {
            it.copy(showFeedback = true, lastAnswerCorrect = correct)
        }

        viewModelScope.launch {
            kotlinx.coroutines.delay(600)
            if (updated.isComplete) {
                finishQuiz(updated)
            } else {
                refreshFromSession()
            }
        }
    }

    private fun finishQuiz(session: com.droidkaigi.quiz.core.domain.model.QuizSession) {
        val result = QuizScorer.scoreSession(session, deps.instantProvider.nowEpochMillis())
        deps.sessionHolder.lastResult = result
        viewModelScope.launch {
            deps.submitScoreUseCase(result)
            _events.emit(QuizEvent.NavigateToResult)
        }
    }

    private fun buildAnswer(question: Question, state: QuizUiState) = when (question) {
        is SingleChoice -> state.selectedSingleId?.let { SingleChoiceAnswer(question.id, it) }
        is MultipleChoice -> MultipleChoiceAnswer(question.id, state.selectedMultipleIds)
        is Reorder -> ReorderAnswer(question.id, state.reorderIds)
    }
}
