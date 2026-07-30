package com.droidkaigi.quiz.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import kotlin.js.ExperimentalWasmJsInterop

@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UnusedParameter")
private fun setCustomLocale(value: String?) {
    js("window.__customLocale = value")
}

actual object LocalAppLocale {
    private val LocalAppLocaleComposition = staticCompositionLocalOf { Locale.current }

    actual val current: String
        @Composable get() = LocalAppLocaleComposition.current.toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        setCustomLocale(value?.replace('_', '-'))
        return LocalAppLocaleComposition.provides(Locale.current)
    }
}
