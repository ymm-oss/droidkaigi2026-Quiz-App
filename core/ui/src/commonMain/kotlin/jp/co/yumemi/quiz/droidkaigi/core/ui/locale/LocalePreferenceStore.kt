package jp.co.yumemi.quiz.droidkaigi.core.ui.locale

import androidx.compose.runtime.Composable

interface LocalePreferenceStore {
    fun load(): AppLocalePreference
    fun save(preference: AppLocalePreference)
}

@Composable
expect fun rememberLocalePreferenceStore(): LocalePreferenceStore
