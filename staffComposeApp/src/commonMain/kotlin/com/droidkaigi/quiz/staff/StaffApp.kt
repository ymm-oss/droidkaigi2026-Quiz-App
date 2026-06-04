package com.droidkaigi.quiz.staff

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.di.initStaffQuizAppGraph
import com.droidkaigi.quiz.feature.staff.StaffShell
import com.droidkaigi.quiz.feature.staff.auth.StaffAuthScreen
import com.droidkaigi.quiz.feature.staff.auth.StaffAuthViewModel

@Composable
fun StaffApp() {
    remember { initStaffQuizAppGraph() }
    QuizTheme {
        val authViewModel: StaffAuthViewModel = viewModel { StaffAuthViewModel() }
        val authState by authViewModel.uiState.collectAsState()
        if (authState.isAuthenticated) {
            StaffShell(onSignOut = { authViewModel.onSignOut() })
        } else {
            StaffAuthScreen(viewModel = authViewModel)
        }
    }
}
