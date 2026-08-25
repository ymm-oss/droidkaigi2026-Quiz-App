package jp.co.yumemi.quiz.droidkaigi.feature.staff

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.firestore.FirestoreErrorMessages
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
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
    val isCreatingFolder: Boolean = false,
    val editingFolderId: String? = null,
    val isUpdatingFolder: Boolean = false,
    val deletingFolderId: String? = null,
    val isDeletingFolder: Boolean = false,
    val showPublishFolderConfirm: Boolean = false,
    val isPublishingFolder: Boolean = false,
    val showSitePublishConfirm: Boolean = false,
    val isTogglingSitePublished: Boolean = false,
) {
    val editingFolder: QuizFolder?
        get() = folders.find { it.id == editingFolderId }

    val deletingFolder: QuizFolder?
        get() = folders.find { it.id == deletingFolderId }
}

sealed interface StaffShellIntent {
    data object Refresh : StaffShellIntent
    data class SelectFolder(val folderId: String) : StaffShellIntent
    data object ShowCreateFolderDialog : StaffShellIntent
    data object DismissCreateFolderDialog : StaffShellIntent
    data class CreateFolder(val name: String, val description: String) : StaffShellIntent
    data class ShowEditFolderDialog(val folderId: String) : StaffShellIntent
    data object DismissEditFolderDialog : StaffShellIntent
    data class UpdateFolder(val folderId: String, val name: String, val description: String) : StaffShellIntent
    data class RequestDeleteFolder(val folderId: String) : StaffShellIntent
    data object DismissDeleteFolderDialog : StaffShellIntent
    data object ConfirmDeleteFolder : StaffShellIntent
    data object RequestPublishFolder : StaffShellIntent
    data object DismissPublishFolderConfirm : StaffShellIntent
    data object ConfirmPublishFolder : StaffShellIntent
    data object RequestToggleSitePublished : StaffShellIntent
    data object DismissSitePublishConfirm : StaffShellIntent
    data object ConfirmToggleSitePublished : StaffShellIntent
}

class StaffShellViewModel(private val deps: AppDependencies = AppDependencies.shared) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffShellUiState())
    val uiState: StateFlow<StaffShellUiState> = _uiState.asStateFlow()

    init {
        refresh()
    }

    fun onIntent(intent: StaffShellIntent) {
        when (intent) {
            StaffShellIntent.Refresh -> refresh()

            is StaffShellIntent.SelectFolder -> _uiState.update { it.copy(selectedFolderId = intent.folderId) }

            StaffShellIntent.RequestPublishFolder ->
                _uiState.update { it.copy(showPublishFolderConfirm = true, errorMessage = null) }

            StaffShellIntent.DismissPublishFolderConfirm ->
                if (!_uiState.value.isPublishingFolder) {
                    _uiState.update { it.copy(showPublishFolderConfirm = false) }
                }

            StaffShellIntent.ConfirmPublishFolder -> publishSelected()

            StaffShellIntent.RequestToggleSitePublished ->
                _uiState.update { it.copy(showSitePublishConfirm = true) }

            StaffShellIntent.DismissSitePublishConfirm ->
                if (!_uiState.value.isTogglingSitePublished) {
                    _uiState.update { it.copy(showSitePublishConfirm = false) }
                }

            StaffShellIntent.ConfirmToggleSitePublished -> toggleSitePublished()

            else -> handleFolderIntent(intent)
        }
    }

    private fun handleFolderIntent(intent: StaffShellIntent) {
        when (intent) {
            StaffShellIntent.ShowCreateFolderDialog ->
                _uiState.update { it.copy(showCreateFolderDialog = true, errorMessage = null) }

            StaffShellIntent.DismissCreateFolderDialog ->
                if (!_uiState.value.isCreatingFolder) {
                    _uiState.update { it.copy(showCreateFolderDialog = false) }
                }

            is StaffShellIntent.CreateFolder -> createFolder(intent.name, intent.description)

            is StaffShellIntent.ShowEditFolderDialog ->
                _uiState.update { it.copy(editingFolderId = intent.folderId, errorMessage = null) }

            StaffShellIntent.DismissEditFolderDialog ->
                if (!_uiState.value.isUpdatingFolder) {
                    _uiState.update { it.copy(editingFolderId = null) }
                }

            is StaffShellIntent.UpdateFolder ->
                updateFolder(intent.folderId, intent.name, intent.description)

            is StaffShellIntent.RequestDeleteFolder -> {
                if (_uiState.value.isDeletingFolder) return
                _uiState.update { it.copy(deletingFolderId = intent.folderId, errorMessage = null) }
            }

            StaffShellIntent.DismissDeleteFolderDialog ->
                if (!_uiState.value.isDeletingFolder) {
                    _uiState.update { it.copy(deletingFolderId = null) }
                }

            StaffShellIntent.ConfirmDeleteFolder -> confirmDeleteFolder()

            else -> error("Unhandled staff shell intent: $intent")
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
                    it.copy(isLoading = false, errorMessage = firestoreErrorMessage(error, "読み込みに失敗しました"))
                }
            }
        }
    }

    private fun createFolder(name: String, description: String) {
        if (name.isBlank() || _uiState.value.isCreatingFolder) return
        viewModelScope.launch {
            staffLog("createFolder start name=$name")
            _uiState.update { it.copy(isCreatingFolder = true, errorMessage = null) }
            try {
                runCatching {
                    deps.createQuizFolderUseCase(name.trim(), description.trim())
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
                        it.copy(errorMessage = firestoreErrorMessage(error, "フォルダの作成に失敗しました"))
                    }
                }
            } finally {
                _uiState.update { it.copy(isCreatingFolder = false) }
            }
        }
    }

    private fun updateFolder(folderId: String, name: String, description: String) {
        if (name.isBlank() || _uiState.value.isUpdatingFolder) return
        val current = _uiState.value.folders.find { it.id == folderId } ?: return
        val updated = current.copy(name = name.trim(), description = description.trim())
        if (updated == current) {
            _uiState.update { it.copy(editingFolderId = null) }
            return
        }
        viewModelScope.launch {
            staffLog("updateFolder start id=$folderId name=$name")
            _uiState.update { it.copy(isUpdatingFolder = true, errorMessage = null) }
            try {
                runCatching { deps.updateQuizFolderUseCase(updated) }
                    .onSuccess {
                        _uiState.update { state -> state.copy(editingFolderId = null, errorMessage = null) }
                        refresh()
                    }
                    .onFailure { error ->
                        staffLog("updateFolder failed: ${error.message}")
                        _uiState.update {
                            it.copy(errorMessage = firestoreErrorMessage(error, "フォルダの更新に失敗しました"))
                        }
                    }
            } finally {
                _uiState.update { it.copy(isUpdatingFolder = false) }
            }
        }
    }

    private fun confirmDeleteFolder() {
        if (_uiState.value.isDeletingFolder) return
        val current = _uiState.value
        val folderId = current.deletingFolderId
        when {
            folderId == null -> Unit

            current.folders.none { it.id == folderId } ->
                _uiState.update { it.copy(deletingFolderId = null) }

            // Keep at least one folder so activeFolderId / participant start stay valid.
            current.folders.size <= 1 ->
                _uiState.update {
                    it.copy(
                        deletingFolderId = null,
                        errorMessage = "最後のフォルダは削除できません",
                    )
                }

            else -> viewModelScope.launch {
                staffLog("deleteFolder start id=$folderId")
                _uiState.update { it.copy(isDeletingFolder = true, errorMessage = null) }
                try {
                    runCatching { deps.deleteQuizFolderUseCase(folderId) }
                        .onSuccess {
                            _uiState.update { state ->
                                val remaining = state.folders.filterNot { it.id == folderId }
                                state.copy(
                                    folders = remaining,
                                    deletingFolderId = null,
                                    selectedFolderId = state.selectedFolderId
                                        ?.takeUnless { it == folderId }
                                        ?: remaining.firstOrNull()?.id,
                                    editingFolderId = state.editingFolderId?.takeUnless { it == folderId },
                                    activeFolderId = state.activeFolderId
                                        ?.takeUnless { it == folderId }
                                        ?: remaining.firstOrNull()?.id,
                                    errorMessage = null,
                                )
                            }
                            refresh()
                        }
                        .onFailure { error ->
                            staffLog("deleteFolder failed: ${error.message}")
                            _uiState.update {
                                it.copy(errorMessage = firestoreErrorMessage(error, "フォルダの削除に失敗しました"))
                            }
                        }
                } finally {
                    _uiState.update { it.copy(isDeletingFolder = false) }
                }
            }
        }
    }

    private fun toggleSitePublished() {
        if (_uiState.value.isTogglingSitePublished) return
        val next = !_uiState.value.sitePublished
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isTogglingSitePublished = true,
                    showSitePublishConfirm = false,
                    errorMessage = null,
                )
            }
            try {
                runCatching { deps.setSitePublishedUseCase(next) }
                    .onSuccess { refresh() }
                    .onFailure { error ->
                        staffLog("setSitePublished failed: ${error.message}")
                        _uiState.update {
                            it.copy(errorMessage = firestoreErrorMessage(error, "サイト公開状態の更新に失敗しました"))
                        }
                    }
            } finally {
                _uiState.update { it.copy(isTogglingSitePublished = false) }
            }
        }
    }

    private companion object {
        fun staffLog(message: String) {
            println("[StaffShell] $message")
        }

        fun firestoreErrorMessage(error: Throwable, fallback: String): String =
            FirestoreErrorMessages.from(error, fallback)
    }

    private fun publishSelected() {
        if (_uiState.value.isPublishingFolder) return
        val folderId = _uiState.value.selectedFolderId ?: return
        viewModelScope.launch {
            staffLog("publishFolder start id=$folderId")
            _uiState.update { it.copy(isPublishingFolder = true, errorMessage = null) }
            try {
                runCatching { deps.setActiveQuizFolderUseCase(folderId) }
                    .onSuccess {
                        _uiState.update { it.copy(showPublishFolderConfirm = false) }
                        refresh()
                    }
                    .onFailure { error ->
                        staffLog("publishFolder failed: ${error.message}")
                        _uiState.update {
                            it.copy(errorMessage = firestoreErrorMessage(error, "フォルダの公開に失敗しました"))
                        }
                    }
            } finally {
                _uiState.update { it.copy(isPublishingFolder = false) }
            }
        }
    }

    private data class RefreshPayload(
        val folders: List<QuizFolder>,
        val activeId: String?,
        val selected: String?,
        val sitePublished: Boolean,
    )
}
