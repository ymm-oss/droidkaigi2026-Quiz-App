package com.droidkaigi.quiz

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlinx.coroutines.Dispatchers

fun main() {
  prepareDesktopFirebaseMainDispatcher()
  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "DroidKaigi 2026 Quiz",
    ) {
      App()
    }
  }
}

private fun prepareDesktopFirebaseMainDispatcher() {
  // GitLive Firebase on JVM (Android SDK bridge) posts Auth listeners to Dispatchers.Main.
  Dispatchers.Main
}
