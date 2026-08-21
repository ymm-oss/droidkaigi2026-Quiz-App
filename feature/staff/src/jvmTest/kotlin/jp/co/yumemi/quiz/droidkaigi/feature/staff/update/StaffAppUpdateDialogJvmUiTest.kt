package jp.co.yumemi.quiz.droidkaigi.feature.staff.update

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.StaffAppRelease
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizStaffTheme
import jp.co.yumemi.quiz.droidkaigi.feature.staff.captureSurfacePng
import kotlin.test.Test

class StaffAppUpdateDialogJvmUiTest {
    private val sampleRelease = StaffAppRelease(
        version = "1.2.0",
        versionCode = 10_200,
        storagePath = "releases/staff-desktop/1.2.0.dmg",
        sha256 = "abc",
        releaseNotes = "スタッフ Desktop の自動更新通知を追加しました。",
    )

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun staffAppUpdate_available_capturesScreenshot() = runDesktopComposeUiTest(
        width = 960,
        height = 720,
    ) {
        setContent {
            QuizStaffTheme {
                StaffAppUpdateDialog(
                    state = StaffAppUpdateUiState(
                        showDialog = true,
                        release = sampleRelease,
                    ),
                    onIntent = {},
                )
            }
        }

        onNodeWithText("新しいバージョンがあります").assertIsDisplayed()
        onNodeWithText("ダウンロード").assertIsDisplayed()
        captureSurfacePng("staff-app-update-available.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun staffAppUpdate_downloading_capturesScreenshot() = runDesktopComposeUiTest(
        width = 960,
        height = 720,
    ) {
        setContent {
            QuizStaffTheme {
                StaffAppUpdateDialog(
                    state = StaffAppUpdateUiState(
                        showDialog = true,
                        release = sampleRelease,
                        isDownloading = true,
                        downloadBytesRead = 50L,
                        downloadTotalBytes = 100L,
                    ),
                    onIntent = {},
                )
            }
        }

        onNodeWithText("新しいバージョンがあります").assertIsDisplayed()
        onNodeWithText("ダウンロード中").assertIsDisplayed()
        captureSurfacePng("staff-app-update-downloading.png")
    }
}
