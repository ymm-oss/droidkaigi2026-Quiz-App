package jp.co.yumemi.quiz.droidkaigi.core.ui.locale

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import java.util.Locale

private var androidDefaultLocale: Locale? = null

actual object LocalAppLocale {
    actual val current: String
        @Composable get() = Locale.getDefault().toString()

    @Composable
    actual infix fun provides(value: String?): ProvidedValue<*> {
        val configuration = Configuration(LocalConfiguration.current)
        val systemDefault = androidDefaultLocale ?: Locale.getDefault().also { androidDefaultLocale = it }
        val new = if (value == null) {
            systemDefault
        } else {
            Locale.forLanguageTag(value)
        }
        Locale.setDefault(new)
        configuration.setLocale(new)
        val resources = LocalContext.current.resources
        @Suppress("DEPRECATION")
        resources.updateConfiguration(configuration, resources.displayMetrics)
        return LocalConfiguration.provides(configuration)
    }
}
