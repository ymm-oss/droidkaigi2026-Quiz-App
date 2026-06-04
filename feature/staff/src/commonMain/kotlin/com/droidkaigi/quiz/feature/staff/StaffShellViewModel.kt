package com.droidkaigi.quiz.feature.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class StaffShellUiState(
    val folders: List<QuizFolder> = emptyList(),
    val selectedFolderId: String? = null,
    val activeFolderId: String? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showCreateFolderDialog: Boolean = false,
)

sealed interface StaffShellIntent {
    data object Refresh : StaffShellIntent
    data class SelectFolder(val folderId: String) : StaffShellIntent
    data object ShowCreateFolderDialog : StaffShellIntent
    data object DismissCreateFolderDialog : StaffShellIntent
    data class CreateFolder(val name: String, val description: String) : StaffShellIntent
    data object PublishSelectedFolder : StaffShellIntent
}

class StaffShellViewModel(
    private val deps: AppDependencies = AppDependencies.shared,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffShellUiState())
    val uiState: StateFlow<StaffShellUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onIntent(intent: StaffShellIntent) {
        when (intent) {
            StaffShellIntent.Refresh -> refresh()
            is StaffShellIntent.SelectFolder -> _uiState.update { it.copy(selectedFolderId = intent.folderId) }
            StaffShellIntent.ShowCreateFolderDialog -> _uiState.update { it.copy(showCreateFolderDialog = true) }
            StaffShellIntent.DismissCreateFolderDialog -> _uiState.update { it.copy(showCreateFolderDialog = false) }
            is StaffShellIntent.CreateFolder -> createFolder(intent.name, intent.description)
            StaffShellIntent.PublishSelectedFolder -> publishSelected()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                deps.quizRepository.getDefaultQuizSet()
                val folders = deps.listQuizFoldersUseCase()
                val activeId = deps.getActiveQuizFolderIdUseCase()
                val selected = _uiState.value.selectedFolderId
                    ?: activeId.takeIf { id -> folders.any { it.id == id } }
                    ?: folders.firstOrNull()?.id
                Triple(folders, activeId, selected)
            }.onSuccess { (folders, activeId, selected) ->
                _uiState.update {
                    it.copy(
                        folders = folders,
                        activeFolderId = activeId,
                        selectedFolderId = selected,
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "読み込みに失敗しました")
                }
            }
        }
    }

    private fun createFolder(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            runCatching {
                val folder = deps.createQuizFolderUseCase(name.trim(), description.trim())
                folder
            }.onSuccess { folder ->
                _uiState.update {
                    it.copy(
                        showCreateFolderDialog = false,
                        selectedFolderId = folder.id,
                    )
                }
                refresh()
            }
        }
    }

    private fun publishSelected() {
        val folderId = _uiState.value.selectedFolderId ?: return
        viewModelScope.launch {
            runCatching { deps.setActiveQuizFolderUseCase(folderId) }
                .onSuccess { refresh() }
        }
    }
}
