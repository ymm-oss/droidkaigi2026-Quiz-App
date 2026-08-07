package com.droidkaigi.quiz.feature.staff.update

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.droidkaigi.quiz.core.data.AppDependencies
import com.droidkaigi.quiz.core.domain.model.StaffAppUpdateStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StaffAppUpdateViewModel(
    private val deps: AppDependencies = AppDependencies.shared,
) : ViewModel() {
    private val _uiState = MutableStateFlow(StaffAppUpdateUiState())
    val uiState: StateFlow<StaffAppUpdateUiState> = _uiState.asStateFlow()

    init {
        checkForUpdate()
    }

    fun onIntent(intent: StaffAppUpdateIntent) {
        when (intent) {
            StaffAppUpdateIntent.Dismiss ->
                _uiState.update {
                    it.copy(showDialog = false, errorMessage = null)
                }
            StaffAppUpdateIntent.Download -> download()
        }
    }

    private fun checkForUpdate() {
        viewModelScope.launch {
            runCatching { deps.checkForStaffAppUpdateUseCase() }
                .onSuccess { status ->
                    when (status) {
                        is StaffAppUpdateStatus.UpdateAvailable ->
                            _uiState.update {
                                it.copy(
                                    showDialog = true,
                                    release = status.release,
                                    errorMessage = null,
                                )
                            }
                        StaffAppUpdateStatus.UpToDate,
                        StaffAppUpdateStatus.Unavailable,
                        -> Unit
                    }
                }
                .onFailure { error ->
                    println("[StaffAppUpdate] check failed: ${error.message}")
                }
        }
    }

    private fun download() {
        val release = _uiState.value.release ?: return
        if (_uiState.value.isDownloading) return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isDownloading = true,
                    downloadBytesRead = 0L,
                    downloadTotalBytes = null,
                    downloadedPath = null,
                    errorMessage = null,
                )
            }
            val result = deps.downloadStaffAppUpdateUseCase(
                release = release,
                onProgress = { read, total ->
                    _uiState.update {
                        it.copy(downloadBytesRead = read, downloadTotalBytes = total)
                    }
                },
                openAfterDownload = true,
            )
            result
                .onSuccess { path ->
                    _uiState.update {
                        it.copy(
                            isDownloading = false,
                            downloadedPath = path,
                            showDialog = false,
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update {
                        it.copy(
                            isDownloading = false,
                            errorMessage = error.message ?: "ダウンロードに失敗しました",
                        )
                    }
                }
        }
    }
}
