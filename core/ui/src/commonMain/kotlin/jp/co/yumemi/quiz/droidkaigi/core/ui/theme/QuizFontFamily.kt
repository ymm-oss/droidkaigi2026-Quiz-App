package jp.co.yumemi.quiz.droidkaigi.core.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

/**
 * Font applied to the whole theme, or null to keep the platform default.
 *
 * Skia on Wasm ships no CJK-capable fallback, so the web build bundles Noto Sans JP.
 * Android and JVM resolve Japanese from system fonts and stay on their defaults.
 */
@Composable
expect fun quizFontFamily(): FontFamily?
