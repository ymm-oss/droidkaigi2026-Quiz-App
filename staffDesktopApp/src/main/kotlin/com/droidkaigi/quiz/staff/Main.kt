package com.droidkaigi.quiz.staff

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "DroidKaigi 2026 Quiz — Staff",
        state = rememberWindowState(width = 960.dp, height = 720.dp),
    ) {
        StaffApp()
    }
}
