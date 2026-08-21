package jp.co.yumemi.quiz.droidkaigi.core.ui.locale

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidedValue

/**
 * Platform locale override for Compose Resources ([stringResource]).
 * See https://kotlinlang.org/docs/multiplatform/compose-resource-environment.html
 */
expect object LocalAppLocale {
    val current: String
        @Composable get

    @Composable
    infix fun provides(value: String?): ProvidedValue<*>
}
