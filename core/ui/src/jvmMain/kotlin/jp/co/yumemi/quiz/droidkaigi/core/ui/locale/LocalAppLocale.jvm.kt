package jp.co.yumemi.quiz.droidkaigi.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.runtime.staticCompositionLocalOf
import java.util.Locale

private var jvmDefaultLocale: Locale? = null
private val JvmLocalAppLocale = staticCompositionLocalOf { Locale.getDefault().toString() }

actual object LocalAppLocale {
    actual val current: String
        @Composable get() = JvmLocalAppLocale.current

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val systemDefault = jvmDefaultLocale ?: Locale.getDefault().also { jvmDefaultLocale = it }
        val new = if (value == null) {
            systemDefault
        } else {
            Locale.forLanguageTag(value)
        }
        Locale.setDefault(new)
        return JvmLocalAppLocale.provides(new.toString())
    }
}
