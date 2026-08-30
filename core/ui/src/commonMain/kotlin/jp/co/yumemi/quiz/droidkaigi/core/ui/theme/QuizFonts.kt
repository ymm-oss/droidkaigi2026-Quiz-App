package jp.co.yumemi.quiz.droidkaigi.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/**
 * Theme font together with whether it can already draw text.
 *
 * @property fontFamily font applied to the whole theme, or null to keep the platform default.
 * @property isReady false while a bundled font is still loading — text drawn in that window falls
 * back to a font that may lack the glyphs the UI needs.
 */
data class QuizFonts(val fontFamily: FontFamily?, val isReady: Boolean)

/**
 * Font applied to the whole theme.
 *
 * Skia on Wasm ships no CJK-capable fallback, so the web build bundles Noto Sans JP and reports
 * [QuizFonts.isReady] only once the file is decoded. Android and JVM resolve Japanese from system
 * fonts and stay on their defaults, so they are ready from the first composition.
 */
@Composable
expect fun rememberQuizFonts(): QuizFonts
