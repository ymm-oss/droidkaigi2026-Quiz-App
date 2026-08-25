package jp.co.yumemi.quiz.droidkaigi.core.data

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * App-scoped cache of participant site publish status.
 * Updated by [jp.co.yumemi.quiz.droidkaigi.feature.quiz.home.HomeViewModel]; read by nav shell and Result.
 */
class SiteStatusHolder {
    private val _sitePublished = MutableStateFlow<Boolean?>(null)
    val sitePublished: StateFlow<Boolean?> = _sitePublished.asStateFlow()

    fun updateSitePublished(value: Boolean?) {
        _sitePublished.value = value
    }

    /** Ranking nav and deep links are available only when staff has published the site. */
    val isRankingNavVisible: Boolean
        get() = _sitePublished.value == true
}
