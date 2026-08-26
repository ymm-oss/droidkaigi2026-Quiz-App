package jp.co.yumemi.quiz.droidkaigi

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import jp.co.yumemi.quiz.droidkaigi.staff.StaffApp
import kotlinx.browser.window

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    ComposeViewport(viewportContainerId = "webApp") {
        if (isStaffPath(window.location.pathname)) {
            StaffApp()
        } else {
            App()
        }
    }
}

internal fun isStaffPath(pathname: String): Boolean {
    val path = pathname.trimEnd('/')
    return path == "/staff" || path.endsWith("/staff")
}
