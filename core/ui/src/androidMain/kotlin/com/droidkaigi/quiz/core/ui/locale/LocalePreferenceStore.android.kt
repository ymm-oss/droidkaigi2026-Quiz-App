package com.droidkaigi.quiz.core.ui.locale

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

private class AndroidLocalePreferenceStore(
    context: Context,
) : LocalePreferenceStore {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun load(): AppLocalePreference =
        AppLocalePreference.fromStorageKey(prefs.getString(KEY_LOCALE, null))

    override fun save(preference: AppLocalePreference) {
        prefs.edit().putString(KEY_LOCALE, preference.storageKey).apply()
    }

    private companion object {
        const val PREFS_NAME = "quiz_locale"
        const val KEY_LOCALE = "locale"
    }
}

@Composable
actual fun rememberLocalePreferenceStore(): LocalePreferenceStore {
    val context = LocalContext.current.applicationContext
    return remember(context) { AndroidLocalePreferenceStore(context) }
}
