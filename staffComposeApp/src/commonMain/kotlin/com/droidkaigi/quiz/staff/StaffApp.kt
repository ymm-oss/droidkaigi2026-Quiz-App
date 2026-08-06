package com.droidkaigi.quiz.staff

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.ui.theme.QuizStaffTheme
import com.droidkaigi.quiz.di.initStaffQuizAppGraph
import com.droidkaigi.quiz.feature.staff.StaffShell
import com.droidkaigi.quiz.feature.staff.auth.StaffAuthScreen
import com.droidkaigi.quiz.feature.staff.auth.StaffAuthViewModel
import com.droidkaigi.quiz.staff.preview.StaffQuizPreviewDialog

@Composable
fun StaffApp() {
    remember { initStaffQuizAppGraph() }
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
