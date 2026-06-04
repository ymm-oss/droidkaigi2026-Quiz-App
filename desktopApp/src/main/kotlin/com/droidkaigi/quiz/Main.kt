package com.droidkaigi.quiz

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DroidKaigi 2026 Quiz",
    ) {
        App()
    }
}
