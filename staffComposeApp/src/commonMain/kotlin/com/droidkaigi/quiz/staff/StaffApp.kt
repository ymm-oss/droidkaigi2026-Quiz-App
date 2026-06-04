package com.droidkaigi.quiz.staff

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.di.initStaffQuizAppGraph
import com.droidkaigi.quiz.feature.staff.StaffShell

@Composable
fun StaffApp() {
    remember { initStaffQuizAppGraph() }
    QuizTheme {
        StaffShell()
    }
}
