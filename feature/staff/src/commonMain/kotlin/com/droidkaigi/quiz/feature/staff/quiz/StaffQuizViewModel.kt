package com.droidkaigi.quiz.feature.staff.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidkaigi.quiz.core.data.AppDependencies
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StaffQuizViewModel(private val folderId: String, private val deps: AppDependencies = AppDependencies.shared) :
    ViewModel() {
    private val _uiState = MutableStateFlow(StaffQuizUiState())
    val uiState: StateFlow<StaffQuizUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onIntent(intent: StaffQuizIntent) {
        when (intent) {
            StaffQuizIntent.Refresh -> refresh()

            StaffQuizIntent.AddQuestion -> openNewEditor()

            is StaffQuizIntent.EditQuestion -> _uiState.update {
                it.copy(editorDraft = intent.question.toDraft(), isNewQuestion = false)
            }

            is StaffQuizIntent.DeleteQuestion -> deleteQuestion(intent.questionId)

            StaffQuizIntent.DismissEditor -> _uiState.update { it.copy(editorDraft = null) }

            is StaffQuizIntent.UpdateEditorDraft -> _uiState.update { it.copy(editorDraft = intent.draft) }

            StaffQuizIntent.SaveEditor -> saveEditor()

            is StaffQuizIntent.ReorderQuestions -> reorderQuestions(intent.fromIndex, intent.toIndex)
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching { deps.getQuizSetForFolderUseCase(folderId) }
                .onSuccess { quizSet ->
                    _uiState.update {
                        it.copy(quizSet = quizSet, isLoading = false, errorMessage = null)
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            quizSet = null,
                            isLoading = false,
                            errorMessage = error.message ?: "Failed to load quiz set",
                        )
                    }
                }
        }
    }

    private fun openNewEditor() {
        val questions = _uiState.value.quizSet?.questions.orEmpty()
        val defaultChoices = defaultItems()
        _uiState.update {
            it.copy(
                isNewQuestion = true,
                editorDraft = StaffQuestionDraft(
                    id = nextAutoQuestionId(questions),
                    prompt = "",
                    explanationMarkdown = "",
                    type = StaffQuestionType.SingleChoice,
                    items = defaultChoices,
                    correctSingleId = defaultChoices.first().id,
                ),
            )
        }
    }

    private fun reorderQuestions(fromIndex: Int, toIndex: Int) {
        val quizSet = _uiState.value.quizSet ?: return
        if (fromIndex !in quizSet.questions.indices ||
            toIndex !in quizSet.questions.indices ||
            fromIndex == toIndex
        ) {
            return
        }
        val mutable = quizSet.questions.toMutableList()
        val item = mutable.removeAt(fromIndex)
        mutable.add(toIndex, item)
        persist(quizSet.copy(questions = mutable)) {}
    }

    private fun saveEditor() {
        val draft = _uiState.value.editorDraft ?: return
        val quizSet = _uiState.value.quizSet ?: return
        val resolvedDraft = if (_uiState.value.isNewQuestion && quizSet.questions.any { it.id == draft.id }) {
            draft.copy(id = nextAutoQuestionId(quizSet.questions))
        } else {
            draft
        }
        val question = runCatching { resolvedDraft.toQuestion() }.getOrElse { error ->
            _uiState.update { state -> state.copy(errorMessage = error.message) }
            return
        }
        val questions = if (_uiState.value.isNewQuestion) {
            quizSet.questions + question
        } else {
            quizSet.questions.map { if (it.id == question.id) question else it }
        }
        persist(quizSet.copy(questions = questions)) {
            _uiState.update { state -> state.copy(editorDraft = null) }
        }
    }

    private fun deleteQuestion(questionId: String) {
        val quizSet = _uiState.value.quizSet ?: return
        persist(quizSet.copy(questions = quizSet.questions.filter { it.id != questionId })) {}
    }

    private fun persist(quizSet: com.droidkaigi.quiz.core.domain.model.QuizSet, onSuccess: () -> Unit) {
        viewModelScope.launch {
            runCatching { deps.saveQuizSetUseCase(quizSet) }
                .onSuccess {
                    _uiState.update { state -> state.copy(quizSet = quizSet, errorMessage = null) }
                    onSuccess()
                }
                .onFailure { error ->
                    _uiState.update { state -> state.copy(errorMessage = error.message) }
                }
        }
    }
}
