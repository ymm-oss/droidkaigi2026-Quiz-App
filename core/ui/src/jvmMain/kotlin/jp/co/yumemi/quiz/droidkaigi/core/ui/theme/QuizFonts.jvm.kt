package jp.co.yumemi.quiz.droidkaigi.core.ui.theme

import androidx.compose.runtime.Composable

private val systemFonts = QuizFonts(fontFamily = null, isReady = true)

@Composable
actual fun rememberQuizFonts(): QuizFonts = systemFonts
