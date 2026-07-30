package com.droidkaigi.quiz.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

actual object LocalAppLocale {
    private var default: Locale? = null
    private val LocalAppLocaleComposition = staticCompositionLocalOf { Locale.getDefault().toString() }

    actual val current: String
        @Composable get() = LocalAppLocaleComposition.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val systemDefault = default ?: Locale.getDefault().also { default = it }
        val new = if (value == null) {
            systemDefault
        } else {
            Locale.forLanguageTag(value)
        }
        Locale.setDefault(new)
        return LocalAppLocaleComposition.provides(new.toString())
    }
}
