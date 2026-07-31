package com.droidkaigi.quiz.feature.quiz.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.MultipleChoiceAnswer
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.QuizResult
import com.droidkaigi.quiz.core.domain.model.QuizSession
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.ReorderAnswer
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.domain.model.SingleChoiceAnswer
import com.droidkaigi.quiz.core.domain.scoring.QuizScorer
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class QuizViewModel(private val deps: AppDependencies = AppDependencies.shared) : ViewModel() {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuizEvent>()
    val events: SharedFlow<QuizEvent> = _events.asSharedFlow()

    /** Captured when the last answer is submitted so feedback wait time does not reduce timeBonus. */
    private var finishedAtEpochMillis: Long? = null

    /** Scored once at finish; reused on submit retries so timeBonus stays fixed. */
    private var pendingResult: QuizResult? = null

    init {
        syncFromSession()
    }

    private fun session() = deps.sessionHolder.currentSession

    /** Re-read [QuizSessionHolder.currentSession] (e.g. after a new quiz starts with a reused ViewModel). */
    fun syncFromSession() {
        refreshFromSession()
    }

    private fun refreshFromSession() {
        val session = session() ?: return
        val question = session.currentQuestion
        val total = session.quizSet.questions.size.coerceAtLeast(1)
        finishedAtEpochMillis = null
        pendingResult = null
        _uiState.update {
            QuizUiState(
                prompt = question?.prompt.orEmpty(),
                progress = session.progressLabel,
                progressFraction = session.currentIndex.toFloat() / total,
                question = question,
                selectedSingleId = null,
                selectedMultipleIds = emptySet(),
                reorderIds = (question as? Reorder)?.items?.map { item -> item.id }.orEmpty(),
                canSubmit = (question as? Reorder)?.items?.isNotEmpty() == true,
                showFeedback = false,
                lastAnswerCorrect = null,
                showExitConfirm = it.showExitConfirm,
                isFinishing = false,
                submitPhase = SubmitPhase.Idle,
            )
        }
    }

    fun onIntent(intent: QuizIntent) {
        when (intent) {
            is QuizIntent.SelectSingle -> selectSingle(intent.id)
            is QuizIntent.ToggleMultiple -> toggleMultiple(intent.id)
            is QuizIntent.MoveReorder -> moveReorder(intent.fromIndex, intent.toIndex)
            QuizIntent.SubmitAnswer -> submitAnswerIfAllowed()
            QuizIntent.ContinueAfterFeedback -> continueAfterFeedback()
            QuizIntent.RetrySubmitScore -> retrySubmitScore()
            QuizIntent.RequestExit -> requestExit()
            QuizIntent.DismissExit -> dismissExit()
            QuizIntent.ConfirmExit -> confirmExit()
        }
    }

    private fun selectSingle(id: String) {
        if (_uiState.value.isFinishing) return
        _uiState.update {
            it.copy(selectedSingleId = id, canSubmit = true)
        }
    }

    private fun toggleMultiple(id: String) {
        if (_uiState.value.isFinishing) return
        _uiState.update {
            val next = if (id in it.selectedMultipleIds) {
                it.selectedMultipleIds - id
            } else {
                it.selectedMultipleIds + id
            }
            it.copy(selectedMultipleIds = next, canSubmit = next.isNotEmpty())
        }
    }

    private fun moveReorder(fromIndex: Int, toIndex: Int) {
        if (_uiState.value.isFinishing) return
        val ids = _uiState.value.reorderIds.toMutableList()
        if (fromIndex in ids.indices && toIndex in ids.indices) {
            val item = ids.removeAt(fromIndex)
            ids.add(toIndex, item)
            _uiState.update { it.copy(reorderIds = ids, canSubmit = true) }
        }
    }

    private fun submitAnswerIfAllowed() {
        if (_uiState.value.isFinishing) return
        submitAnswer()
    }

    private fun dismissExit() {
        if (_uiState.value.isFinishing) return
        _uiState.update { it.copy(showExitConfirm = false) }
    }

    /** 最終問回答後〜Result 遷移前は中断ダイアログを出さない。 */
    private fun requestExit() {
        if (_uiState.value.isFinishing) return
        _uiState.update { it.copy(showExitConfirm = true) }
    }

    private fun confirmExit() {
        if (_uiState.value.isFinishing) {
            _uiState.update { it.copy(showExitConfirm = false) }
            return
        }
        deps.sessionHolder.currentSession = null
        _uiState.update { it.copy(showExitConfirm = false) }
        viewModelScope.launch {
            _events.emit(QuizEvent.NavigateHome)
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
        if (updated.isComplete) {
            finishedAtEpochMillis = deps.instantProvider.nowEpochMillis()
        }

        _uiState.update {
            it.copy(
                showFeedback = true,
                lastAnswerCorrect = correct,
                showExitConfirm = false,
                isFinishing = updated.isComplete,
                progress = updated.progressLabel,
                progressFraction = updated.currentIndex.toFloat() /
                    updated.quizSet.questions.size.coerceAtLeast(1),
            )
        }
    }

    private fun continueAfterFeedback() {
        if (!_uiState.value.showFeedback) return
        if (_uiState.value.submitPhase == SubmitPhase.Submitting) return
        val session = session() ?: return
        if (session.isComplete) {
            submitScoreAndFinish(session)
        } else {
            _uiState.update { it.copy(showFeedback = false) }
            refreshFromSession()
        }
    }

    private fun retrySubmitScore() {
        if (_uiState.value.submitPhase != SubmitPhase.Failed) return
        val session = session() ?: return
        if (!session.isComplete) return
        submitScoreAndFinish(session)
    }

    private fun submitScoreAndFinish(session: QuizSession) {
        if (_uiState.value.submitPhase == SubmitPhase.Submitting) return
        val finishedAt = finishedAtEpochMillis ?: deps.instantProvider.nowEpochMillis().also {
            finishedAtEpochMillis = it
        }
        val result = pendingResult ?: QuizScorer.scoreSession(session, finishedAt).also {
            pendingResult = it
        }
        deps.sessionHolder.lastResult = result
        viewModelScope.launch {
            _uiState.update { it.copy(submitPhase = SubmitPhase.Submitting) }
            try {
                deps.submitScoreUseCase(
                    result = result,
                    folderId = session.folderId,
                    completedAtEpochMillis = finishedAt,
                    startedAtEpochMillis = session.startedAtEpochMillis,
                )
                _uiState.update { it.copy(submitPhase = SubmitPhase.Idle) }
                _events.emit(QuizEvent.NavigateToResult)
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
                _uiState.update { it.copy(submitPhase = SubmitPhase.Failed) }
            }
        }
    }

    private fun buildAnswer(question: Question, state: QuizUiState) = when (question) {
        is SingleChoice -> state.selectedSingleId?.let { SingleChoiceAnswer(question.id, it) }
        is MultipleChoice -> MultipleChoiceAnswer(question.id, state.selectedMultipleIds)
        is Reorder -> ReorderAnswer(question.id, state.reorderIds)
    }
}
