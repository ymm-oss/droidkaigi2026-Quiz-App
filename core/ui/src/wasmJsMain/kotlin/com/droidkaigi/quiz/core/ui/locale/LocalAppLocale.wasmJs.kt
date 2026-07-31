package com.droidkaigi.quiz.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.text.intl.Locale
import kotlin.js.JsName

// Paired with wasmApp index.html navigator.languages override.
// https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html
@Suppress("ClassName")
private external object window {
    @JsName("__customLocale")
    var customLocale: String?
}

private val WasmLocalAppLocale = staticCompositionLocalOf { Locale.current }

actual object LocalAppLocale {
    actual val current: String
        @Composable get() = WasmLocalAppLocale.current.toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        window.customLocale = value?.replace('_', '-')
        return WasmLocalAppLocale.provides(Locale.current)
    }
}
