package com.droidkaigi.quiz.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import kotlin.js.ExperimentalWasmJsInterop

/** Patched via `wasmApp` `index.html` so Compose Resources can override `navigator.languages`. */
@OptIn(ExperimentalWasmJsInterop::class)
@Suppress("UnusedParameter")
private fun setCustomLocale(value: String?): Unit = js("window.__customLocale = value")

private val WasmLocalAppLocale = staticCompositionLocalOf { Locale.current }

actual object LocalAppLocale {
    actual val current: String
        @Composable get() = WasmLocalAppLocale.current.toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        setCustomLocale(value?.replace('_', '-'))
        return WasmLocalAppLocale.provides(Locale.current)
    }
}
