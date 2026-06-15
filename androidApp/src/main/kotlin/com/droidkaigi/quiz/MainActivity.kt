package com.droidkaigi.quiz

import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.ui.graphics.toArgb
import com.droidkaigi.quiz.core.ui.theme.QuizTokens

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        val isDark = resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES
        val bgColor = if (isDark) QuizTokens.surfaceDark else QuizTokens.surface
        window.decorView.setBackgroundColor(bgColor.toArgb())
        setContent {
            App()
        }
    }
}
