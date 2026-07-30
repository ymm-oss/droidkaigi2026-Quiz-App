package com.droidkaigi.quiz.feature.staff

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.feature.staff.folders.StaffFolderSidebar
import com.droidkaigi.quiz.feature.staff.quiz.StaffQuizScreen
import com.droidkaigi.quiz.feature.staff.ranking.StaffRankingScreen

@Composable
fun StaffShell(onSignOut: () -> Unit, shellViewModel: StaffShellViewModel = viewModel { StaffShellViewModel() }) {
    val shellState by shellViewModel.uiState.collectAsState()
    var selectedTab by rememberSaveable { mutableStateOf(StaffTab.Quiz) }
    var newFolderName by rememberSaveable { mutableStateOf("") }
    var newFolderDescription by rememberSaveable { mutableStateOf("") }
    val selectedFolder = shellState.folders.find { it.id == shellState.selectedFolderId }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            StaffTopBar(onSignOut = onSignOut)
            Row(modifier = Modifier.fillMaxSize()) {
                StaffNavigationRail(
                    selectedTab = selectedTab,
                    onSelectTab = { selectedTab = it },
                )
                StaffFolderSidebar(
                    state = shellState,
                    onIntent = shellViewModel::onIntent,
                    newFolderName = newFolderName,
                    onNewFolderNameChange = { newFolderName = it },
                    newFolderDescription = newFolderDescription,
                    onNewFolderDescriptionChange = { newFolderDescription = it },
                )
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
                    if (selectedFolder == null) {
                        StaffNoFolderSelected(modifier = Modifier.align(Alignment.Center))
                    } else {
                        when (selectedTab) {
                            StaffTab.Quiz -> StaffQuizScreen(
                                folderId = selectedFolder.id,
                                folderName = selectedFolder.displayName,
                                folderDescription = selectedFolder.description,
                            )

                            StaffTab.Ranking -> StaffRankingScreen(folderId = selectedFolder.id)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StaffNoFolderSelected(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(QuizTokens.spacingLarge),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Icon(
            imageVector = Icons.Outlined.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp),
        )
        Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
        Text(
            text = "フォルダを選択してください",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
