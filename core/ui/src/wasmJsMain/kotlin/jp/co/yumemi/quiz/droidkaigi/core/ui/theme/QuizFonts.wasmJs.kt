package jp.co.yumemi.quiz.droidkaigi.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.Res
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.noto_sans_jp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.preloadFont

private val themeWeights = listOf(
    FontWeight.Normal,
    FontWeight.Medium,
    FontWeight.SemiBold,
    FontWeight.Bold,
)

/**
 * Draw text with the fallback font rather than hiding the UI forever if the bundled font never
 * arrives (offline cache miss, blocked request).
 */
private const val FONT_LOAD_TIMEOUT_MILLIS = 10_000L

// AppLocaleEnvironment remounts its content when the locale changes. Keep the decoded family
// outside that composition so changing language never reopens the font gate.
private var cachedFontFamily: FontFamily? = null

@OptIn(ExperimentalResourceApi::class)
@Composable
actual fun rememberQuizFonts(): QuizFonts {
    // One variable font file covers every weight through the wght axis, and preloading keeps the
    // first composition from rendering with the CJK-less Skia fallback.
    val fonts = themeWeights.map { weight ->
        preloadFont(
            resource = Res.font.noto_sans_jp,
            weight = weight,
            style = FontStyle.Normal,
            variationSettings = FontVariation.Settings(weight, FontStyle.Normal),
        ).value
    }
    var timedOut by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(FONT_LOAD_TIMEOUT_MILLIS)
        timedOut = true
    }
    val loadedFontFamily = remember(fonts) {
        fonts.takeIf { loaded -> loaded.all { it != null } }
            ?.filterNotNull()
            ?.let(::FontFamily)
    }
    SideEffect {
        if (loadedFontFamily != null) {
            cachedFontFamily = loadedFontFamily
        }
    }
    val fontFamily = loadedFontFamily ?: cachedFontFamily
    return QuizFonts(
        fontFamily = fontFamily,
        isReady = fontFamily != null || timedOut,
    )
}
