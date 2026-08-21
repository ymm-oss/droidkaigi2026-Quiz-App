package jp.co.yumemi.quiz.droidkaigi.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf

class AppLocaleController(private val store: LocalePreferenceStore) {
    var preference by mutableStateOf(store.load())
        private set

    fun select(value: AppLocalePreference) {
        preference = value
        store.save(value)
    }
}

val LocalAppLocaleController = staticCompositionLocalOf<AppLocaleController> {
    error("AppLocaleController not provided. Wrap with AppLocaleEnvironment.")
}

@Composable
fun AppLocaleEnvironment(content: @Composable () -> Unit) {
    val store = rememberLocalePreferenceStore()
    val controller = remember(store) { AppLocaleController(store) }
    val localeTag = controller.preference.localeTag
    CompositionLocalProvider(
        LocalAppLocaleController provides controller,
        LocalAppLocale provides localeTag,
    ) {
        key(localeTag) {
            content()
        }
    }
}
