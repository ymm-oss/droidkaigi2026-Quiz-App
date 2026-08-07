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
import kotlinx.coroutines.withTimeout

private const val STAFF_SHELL_REFRESH_TIMEOUT_MS = 30_000L

data class StaffShellUiState(
    val folders: List<QuizFolder> = emptyList(),
    val selectedFolderId: String? = null,
    val activeFolderId: String? = null,
    val sitePublished: Boolean = false,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
    val showCreateFolderDialog: Boolean = false,
    val showSitePublishConfirm: Boolean = false,
)

sealed interface StaffShellIntent {
    data object Refresh : StaffShellIntent
    data class SelectFolder(val folderId: String) : StaffShellIntent
    data object ShowCreateFolderDialog : StaffShellIntent
    data object DismissCreateFolderDialog : StaffShellIntent
    data class CreateFolder(val name: String, val description: String) : StaffShellIntent
    data object PublishSelectedFolder : StaffShellIntent
    data object RequestToggleSitePublished : StaffShellIntent
    data object DismissSitePublishConfirm : StaffShellIntent
    data object ConfirmToggleSitePublished : StaffShellIntent
}

class StaffShellViewModel(private val deps: AppDependencies = AppDependencies.shared) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffShellUiState())
    val uiState: StateFlow<StaffShellUiState> = _uiState.asStateFlow()
    private var sitePublishInFlight: Boolean = false

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
            StaffShellIntent.RequestToggleSitePublished ->
                _uiState.update { it.copy(showSitePublishConfirm = true) }
            StaffShellIntent.DismissSitePublishConfirm ->
                _uiState.update { it.copy(showSitePublishConfirm = false) }
            StaffShellIntent.ConfirmToggleSitePublished -> toggleSitePublished()
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            staffLog("refresh start")
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            runCatching {
                withTimeout(STAFF_SHELL_REFRESH_TIMEOUT_MS) {
                    val folders = deps.listQuizFoldersUseCase()
                    val activeId = runCatching { deps.getActiveQuizFolderIdUseCase() }
                        .onFailure { staffLog("getActiveQuizFolderId failed: ${it.message}") }
                        .getOrNull()
                    val sitePublished = runCatching { deps.getSitePublishedUseCase() }
                        .onFailure { staffLog("getSitePublished failed: ${it.message}") }
                        .getOrDefault(false)
                    val selected = _uiState.value.selectedFolderId
                        ?: activeId?.takeIf { id -> folders.any { it.id == id } }
                        ?: folders.firstOrNull()?.id
                    staffLog(
                        "refresh ok folders=${folders.size} activeId=$activeId sitePublished=$sitePublished " +
                            "selected=$selected " +
                            folders.joinToString { "${it.id}:${it.displayName}" },
                    )
                    RefreshPayload(folders, activeId, selected, sitePublished)
                }
            }.onSuccess { payload ->
                _uiState.update {
                    it.copy(
                        folders = payload.folders,
                        activeFolderId = payload.activeId,
                        selectedFolderId = payload.selected,
                        sitePublished = payload.sitePublished,
                        isLoading = false,
                    )
                }
            }.onFailure { error ->
                staffLog("refresh failed: ${error.message}")
                error.printStackTrace()
                _uiState.update {
                    it.copy(isLoading = false, errorMessage = error.message ?: "読み込みに失敗しました")
                }
            }
        }
    }

    private fun createFolder(name: String, description: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            staffLog("createFolder start name=$name")
            runCatching {
                val folder = deps.createQuizFolderUseCase(name.trim(), description.trim())
                folder
            }.onSuccess { folder ->
                staffLog("createFolder ok id=${folder.id} displayName=${folder.displayName}")
                _uiState.update {
                    it.copy(
                        showCreateFolderDialog = false,
                        selectedFolderId = folder.id,
                        errorMessage = null,
                    )
                }
                refresh()
            }.onFailure { error ->
                staffLog("createFolder failed: ${error.message}")
                error.printStackTrace()
                _uiState.update {
                    it.copy(errorMessage = error.message ?: "フォルダの作成に失敗しました")
                }
            }
        }
    }

    private fun toggleSitePublished() {
        if (sitePublishInFlight) return
        val next = !_uiState.value.sitePublished
        viewModelScope.launch {
            sitePublishInFlight = true
            _uiState.update { it.copy(showSitePublishConfirm = false) }
            try {
                runCatching { deps.setSitePublishedUseCase(next) }
                    .onSuccess { refresh() }
                    .onFailure { error ->
                        staffLog("setSitePublished failed: ${error.message}")
                        _uiState.update {
                            it.copy(errorMessage = error.message ?: "サイト公開状態の更新に失敗しました")
                        }
                    }
            } finally {
                sitePublishInFlight = false
            }
        }
    }

    private companion object {
        fun staffLog(message: String) {
            println("[StaffShell] $message")
        }
    }

    private fun publishSelected() {
        val folderId = _uiState.value.selectedFolderId ?: return
        viewModelScope.launch {
            runCatching { deps.setActiveQuizFolderUseCase(folderId) }
                .onSuccess { refresh() }
        }
    }

    private data class RefreshPayload(
        val folders: List<QuizFolder>,
        val activeId: String?,
        val selected: String?,
        val sitePublished: Boolean,
    )
}
