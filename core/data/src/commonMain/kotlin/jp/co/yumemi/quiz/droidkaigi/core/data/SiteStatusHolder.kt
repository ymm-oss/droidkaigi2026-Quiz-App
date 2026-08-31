package jp.co.yumemi.quiz.droidkaigi.core.data

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.AppConfigStatus
import jp.co.yumemi.quiz.droidkaigi.core.domain.usecase.ObserveAppConfigUseCase
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val FIRST_SNAPSHOT_TIMEOUT_MS = 15_000L

/**
 * App-scoped cache of participant site publish status and active folder.
 * Filled by [bindAppConfig] (App / StaffApp); read by nav shell, Home, Ranking, staff shell.
 */
class SiteStatusHolder {
    private val _sitePublished = MutableStateFlow<Boolean?>(null)
    val sitePublished: StateFlow<Boolean?> = _sitePublished.asStateFlow()

    private val _activeFolderId = MutableStateFlow<String?>(null)
    val activeFolderId: StateFlow<String?> = _activeFolderId.asStateFlow()

    private val _publishedFolderIds = MutableStateFlow<List<String>>(emptyList())
    val publishedFolderIds: StateFlow<List<String>> = _publishedFolderIds.asStateFlow()

    private val _observeFailed = MutableStateFlow(false)
    val observeFailed: StateFlow<Boolean> = _observeFailed.asStateFlow()

    private val _retryToken = MutableStateFlow(0)
    val retryToken: StateFlow<Int> = _retryToken.asStateFlow()

    fun updateSitePublished(value: Boolean?) {
        _sitePublished.value = value
    }

    fun applyStatus(status: AppConfigStatus) {
        val resolved = status.resolvedPublishedFolderIds
        _sitePublished.value = status.sitePublished
        _publishedFolderIds.value = resolved
        _activeFolderId.value = resolved.firstOrNull() ?: status.activeFolderId
        _observeFailed.value = false
    }

    fun markObserveFailed() {
        _observeFailed.value = true
    }

    fun requestRetry() {
        _observeFailed.value = false
        _retryToken.update { it + 1 }
    }

    /** Ranking nav and deep links are available only when staff has published the site. */
    val isRankingNavVisible: Boolean
        get() = _sitePublished.value == true
}

suspend fun SiteStatusHolder.bindAppConfig(observeAppConfigUseCase: ObserveAppConfigUseCase) {
    coroutineScope {
        val collectJob = launch {
            try {
                observeAppConfigUseCase().collect { applyStatus(it) }
            } catch (e: CancellationException) {
                throw e
            } catch (@Suppress("TooGenericExceptionCaught") _: Exception) {
                markObserveFailed()
            }
        }
        launch {
            delay(FIRST_SNAPSHOT_TIMEOUT_MS)
            if (sitePublished.value == null && !observeFailed.value) {
                markObserveFailed()
            }
        }
        collectJob.join()
    }
}
