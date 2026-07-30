package com.droidkaigi.quiz.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
private fun localStorageGetItem(key: String): String? =
    js("localStorage.getItem(key)")

@OptIn(ExperimentalWasmJsInterop::class)
private fun localStorageSetItem(key: String, value: String) {
    js("localStorage.setItem(key, value)")
}

private class WasmLocalePreferenceStore : LocalePreferenceStore {
    override fun load(): AppLocalePreference =
        AppLocalePreference.fromStorageKey(localStorageGetItem(KEY_LOCALE))

    override fun save(preference: AppLocalePreference) {
        localStorageSetItem(KEY_LOCALE, preference.storageKey)
    }

    private companion object {
        const val KEY_LOCALE = "quiz_locale"
    }
}

@Composable
actual fun rememberLocalePreferenceStore(): LocalePreferenceStore =
    remember { WasmLocalePreferenceStore() }
