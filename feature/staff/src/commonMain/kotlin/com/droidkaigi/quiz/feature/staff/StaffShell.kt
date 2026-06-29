package com.droidkaigi.quiz.feature.staff

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.ui.components.QuizScreenBackground
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.feature.staff.folders.StaffFolderSidebar
import com.droidkaigi.quiz.feature.staff.quiz.StaffQuizScreen
import com.droidkaigi.quiz.feature.staff.ranking.StaffRankingScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffShell(onSignOut: () -> Unit, shellViewModel: StaffShellViewModel = viewModel { StaffShellViewModel() }) {
    val shellState by shellViewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(StaffTab.Quiz) }
    var newFolderName by rememberSaveable { mutableStateOf("") }
    var newFolderDescription by rememberSaveable { mutableStateOf("") }

    QuizScreenBackground {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            text = "DroidKaigi Quiz — スタッフ",
                            style = MaterialTheme.typography.titleLarge,
                        )
                    },
                    actions = {
                        TextButton(onClick = onSignOut) {
                            Text("ログアウト")
                        }
                    },
                )
            },
        ) { padding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
            ) {
                StaffFolderSidebar(
                    state = shellState,
                    onIntent = shellViewModel::onIntent,
                    newFolderName = newFolderName,
                    onNewFolderNameChange = { newFolderName = it },
                    newFolderDescription = newFolderDescription,
                    onNewFolderDescriptionChange = { newFolderDescription = it },
                )
                NavigationRail {
                    NavigationRailItem(
                        selected = selectedTab == StaffTab.Quiz,
                        onClick = { selectedTab = StaffTab.Quiz },
                        icon = {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.List,
                                contentDescription = null,
                            )
                        },
                        label = { Text("クイズ") },
                    )
                    NavigationRailItem(
                        selected = selectedTab == StaffTab.Ranking,
                        onClick = { selectedTab = StaffTab.Ranking },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                            )
                        },
                        label = { Text("ランキング") },
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    val folderId = shellState.selectedFolderId
                    if (folderId == null) {
                        Text(
                            text = "フォルダを選択してください",
                            modifier = Modifier.padding(QuizTokens.spacingLarge),
                        )
                    } else {
                        when (selectedTab) {
                            StaffTab.Quiz -> StaffQuizScreen(folderId = folderId)
                            StaffTab.Ranking -> StaffRankingScreen(folderId = folderId)
                        }
                    }
                }
            }
        }
    }
}
