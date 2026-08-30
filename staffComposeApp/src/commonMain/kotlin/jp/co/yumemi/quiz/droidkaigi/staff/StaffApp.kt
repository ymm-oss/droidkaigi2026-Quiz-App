package jp.co.yumemi.quiz.droidkaigi.staff

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.co.yumemi.quiz.droidkaigi.core.data.AppDependencies
import jp.co.yumemi.quiz.droidkaigi.core.data.bindAppConfig
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizStaffTheme
import jp.co.yumemi.quiz.droidkaigi.di.initStaffQuizAppGraph
import jp.co.yumemi.quiz.droidkaigi.feature.staff.StaffShell
import jp.co.yumemi.quiz.droidkaigi.feature.staff.auth.StaffAuthScreen
import jp.co.yumemi.quiz.droidkaigi.feature.staff.auth.StaffAuthViewModel
import jp.co.yumemi.quiz.droidkaigi.staff.preview.StaffQuizPreviewDialog

@Composable
fun StaffApp() {
    remember { initStaffQuizAppGraph() }
    BindAppConfig()
    QuizStaffTheme {
        val authViewModel: StaffAuthViewModel = viewModel { StaffAuthViewModel() }
        val authState by authViewModel.uiState.collectAsState()
        var previewFolderId by remember { mutableStateOf<String?>(null) }
        if (authState.isAuthenticated) {
            StaffShell(
                onSignOut = {
                    previewFolderId = null
                    authViewModel.onSignOut()
                },
                onPreviewFolder = { folderId -> previewFolderId = folderId },
            )
            previewFolderId?.let { folderId ->
                StaffQuizPreviewDialog(
                    folderId = folderId,
                    onDismiss = { previewFolderId = null },
                )
            }
        } else {
            StaffAuthScreen(viewModel = authViewModel)
        }
    }
}

@Composable
private fun BindAppConfig() {
    val deps = AppDependencies.shared
    val retryToken by deps.siteStatusHolder.retryToken.collectAsState()
    LaunchedEffect(retryToken) {
        deps.siteStatusHolder.bindAppConfig(deps.observeAppConfigUseCase)
    }
}
