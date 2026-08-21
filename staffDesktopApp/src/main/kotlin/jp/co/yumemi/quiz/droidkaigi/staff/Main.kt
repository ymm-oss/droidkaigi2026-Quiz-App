package jp.co.yumemi.quiz.droidkaigi.staff

import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import kotlinx.coroutines.Dispatchers

fun main() {
  prepareDesktopFirebaseMainDispatcher()
  application {
    Window(
      onCloseRequest = ::exitApplication,
      title = "Droid26クイズ管理アプリ",
      state = rememberWindowState(width = 960.dp, height = 720.dp),
    ) {
      StaffApp()
    }
  }
}

private fun prepareDesktopFirebaseMainDispatcher() {
  Dispatchers.Main
}
