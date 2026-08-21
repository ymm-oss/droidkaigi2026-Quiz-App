package jp.co.yumemi.quiz.droidkaigi.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.util.prefs.Preferences

private class JvmLocalePreferenceStore : LocalePreferenceStore {
    private val prefs = Preferences.userRoot().node(NODE)

    override fun load(): AppLocalePreference = AppLocalePreference.fromStorageKey(prefs.get(KEY_LOCALE, null))

    override fun save(preference: AppLocalePreference) {
        prefs.put(KEY_LOCALE, preference.storageKey)
        prefs.flush()
    }

    private companion object {
        const val NODE = "jp/co/yumemi/quiz/droidkaigi/locale"
        const val KEY_LOCALE = "locale"
    }
}

@Composable
actual fun rememberLocalePreferenceStore(): LocalePreferenceStore = remember { JvmLocalePreferenceStore() }
