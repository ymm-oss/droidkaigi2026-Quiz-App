package jp.co.yumemi.quiz.droidkaigi.feature.quiz.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoiceAnswer
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Question
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Reorder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ReorderAnswer
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoiceAnswer
import jp.co.yumemi.quiz.droidkaigi.core.domain.scoring.QuizScorer
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.CompleteQuizResult
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.SubmitQuizAnswerResult
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
    private val submitScore: Boolean = true,
) : ViewModel() {
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<QuizEvent>()
    val events: SharedFlow<QuizEvent> = _events.asSharedFlow()

    /** 最後に同期したセッションの開始時刻。同一プレイ中の再 sync（構成変更後の再 composition）を無視する。 */
    private var syncedSessionKey: Long? = null

    init {
        syncFromSession()
    }

    private fun session() = deps.sessionHolder.currentSession

    /**
     * Re-read [QuizSessionHolder.currentSession] when it points to a different play-through
     * (e.g. after a new quiz starts with a reused ViewModel). No-op for the same session so a
     * configuration change does not wipe unsubmitted selections or the feedback overlay.
     */
    fun syncFromSession() {
        val key = session()?.startedAtEpochMillis
        if (key != null && key == syncedSessionKey) return
        syncedSessionKey = key
        refreshFromSession()
    }

    private fun refreshFromSession() {
        val session = session() ?: return
        if (session.isComplete) {
            val lastQuestion = session.quizSet.questions.lastOrNull()
            val lastAnswer = lastQuestion?.let { session.answers[it.id] }
            val restoredCorrect = if (lastQuestion != null && lastAnswer != null) {
                QuizScorer.isCorrect(lastQuestion, lastAnswer)
            } else {
                _uiState.value.lastAnswerCorrect
            }
            _uiState.update {
                QuizUiState(
                    prompt = "",
                    progress = session.progressLabel,
                    progressFraction = session.progressFraction,
                    question = null,
                    selectedSingleId = null,
                    selectedMultipleIds = emptySet(),
                    reorderIds = emptyList(),
                    canSubmit = false,
                    showFeedback = true,
                    lastAnswerCorrect = restoredCorrect,
                    showExitConfirm = false,
                    isFinishing = true,
                    submitPhase = when {
                        deps.sessionHolder.scoreSubmitInFlight -> SubmitPhase.Submitting

                        // pendingResult is set once scoring runs; cleared on successful upload.
                        deps.sessionHolder.pendingResult != null -> SubmitPhase.Failed

                        else -> SubmitPhase.Idle
                    },
                )
            }
            return
        }
        val question = session.currentQuestion
        _uiState.update {
            QuizUiState(
                prompt = question?.prompt.orEmpty(),
                progress = session.progressLabel,
                progressFraction = session.progressFraction,
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
        deps.sessionHolder.clearPlaySession()
        _uiState.update { it.copy(showExitConfirm = false) }
        viewModelScope.launch {
            _events.emit(QuizEvent.NavigateHome)
        }
    }

    private fun submitAnswer() {
        val question = session()?.currentQuestion ?: return
        val answer = buildAnswer(question, _uiState.value) ?: return
        when (val outcome = deps.quizPlayUseCase.submitAnswer(answer)) {
            is SubmitQuizAnswerResult.Accepted -> {
                _uiState.update {
                    it.copy(
                        showFeedback = true,
                        lastAnswerCorrect = outcome.isCorrect,
                        showExitConfirm = false,
                        isFinishing = outcome.isComplete,
                        progress = outcome.session.progressLabel,
                        progressFraction = outcome.session.progressFraction,
                    )
                }
            }

            SubmitQuizAnswerResult.Rejected -> Unit
        }
    }

    private fun continueAfterFeedback() {
        if (!_uiState.value.showFeedback) return
        if (_uiState.value.submitPhase == SubmitPhase.Submitting) return
        val session = session() ?: return
        if (session.isComplete) {
            submitScoreAndFinish()
        } else {
            refreshFromSession()
        }
    }

    private fun retrySubmitScore() {
        if (_uiState.value.submitPhase != SubmitPhase.Failed) return
        val session = session() ?: return
        if (!session.isComplete) return
        submitScoreAndFinish()
    }

    private fun submitScoreAndFinish() {
        if (_uiState.value.submitPhase == SubmitPhase.Submitting) return
        viewModelScope.launch {
            _uiState.update { it.copy(submitPhase = SubmitPhase.Submitting) }
            when (deps.quizPlayUseCase.completeAndSubmitScore(submitScore = submitScore)) {
                is CompleteQuizResult.Success -> {
                    _uiState.update { it.copy(submitPhase = SubmitPhase.Idle) }
                    _events.emit(QuizEvent.NavigateToResult)
                }

                is CompleteQuizResult.Failure -> {
                    _uiState.update { it.copy(submitPhase = SubmitPhase.Failed) }
                }

                CompleteQuizResult.Ignored -> {
                    if (!deps.sessionHolder.scoreSubmitInFlight) {
                        _uiState.update {
                            it.copy(
                                submitPhase = if (deps.sessionHolder.pendingResult != null) {
                                    SubmitPhase.Failed
                                } else {
                                    SubmitPhase.Idle
                                },
                            )
                        }
                    }
                }
            }
        }
    }

    private fun buildAnswer(question: Question, state: QuizUiState) = when (question) {
        is SingleChoice -> state.selectedSingleId?.let { SingleChoiceAnswer(question.id, it) }
        is MultipleChoice -> MultipleChoiceAnswer(question.id, state.selectedMultipleIds)
        is Reorder -> ReorderAnswer(question.id, state.reorderIds)
    }
}
