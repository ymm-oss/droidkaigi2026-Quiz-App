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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.ReorderItem
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.core.ui.components.QuizScreenBackground
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.feature.staff.auth.StaffAuthContent
import com.droidkaigi.quiz.feature.staff.folders.StaffFolderSidebar
import com.droidkaigi.quiz.feature.staff.quiz.StaffListItem
import com.droidkaigi.quiz.feature.staff.quiz.StaffQuestionDraft
import com.droidkaigi.quiz.feature.staff.quiz.StaffQuestionEditorDialog
import com.droidkaigi.quiz.feature.staff.quiz.StaffQuestionType
import com.droidkaigi.quiz.feature.staff.quiz.StaffQuizContent
import com.droidkaigi.quiz.feature.staff.ranking.StaffRankingContent
import kotlin.test.Test

/**
 * Captures staff console screenshots for design reference (e.g. Stitch).
 * Output: docs/screenshots/staff/ (PNG files).
 */
class StaffScreenshotJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureAuth() = runDesktopComposeUiTest(width = 1280, height = 800) {
        setContent {
            QuizTheme {
                StaffAuthContent(
                    email = "staff@droidkaigi.local",
                    password = "********",
                    isLoading = false,
                    errorMessage = null,
                    showQuickSignIn = true,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignInClick = {},
                    onQuickSignInClick = {},
                )
            }
        }
        onNodeWithText("スタッフログイン").assertIsDisplayed()
        captureSurfacePng("01-auth.png")
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
    @Test
    fun captureConsoleQuiz() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizTheme {
                StaffConsolePreview(
                    selectedTab = StaffTab.Quiz,
                    quizContent = {
                        StaffQuizContent(
                            quizTitle = "Day 1 · Easy",
                            questions = sampleQuestions,
                            isLoading = false,
                            errorMessage = null,
                            onRefresh = {},
                            onAddQuestion = {},
                            onEditQuestion = {},
                            onRequestDeleteQuestion = {},
                            onReorderQuestions = { _, _ -> },
                        )
                    },
                )
            }
        }
        onNodeWithText("問題を追加").assertIsDisplayed()
        captureSurfacePng("02-console-quiz.png")
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
    @Test
    fun captureConsoleRanking() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizTheme {
                StaffConsolePreview(
                    selectedTab = StaffTab.Ranking,
                    quizContent = {
                        StaffRankingContent(
                            entries = sampleRanking,
                            isLoading = false,
                            errorMessage = null,
                            onRefresh = {},
                        )
                    },
                )
            }
        }
        onNodeWithText("本日のランキング").assertIsDisplayed()
        captureSurfacePng("03-console-ranking.png")
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
    @Test
    fun captureQuestionEditor() = runDesktopComposeUiTest(width = 1280, height = 900) {
        setContent {
            QuizTheme {
                QuizScreenBackground {
                    Box(modifier = Modifier.fillMaxSize()) {
                        StaffQuizContent(
                            quizTitle = "Day 1 · Easy",
                            questions = sampleQuestions,
                            isLoading = false,
                            errorMessage = null,
                            onRefresh = {},
                            onAddQuestion = {},
                            onEditQuestion = {},
                            onRequestDeleteQuestion = {},
                            onReorderQuestions = { _, _ -> },
                        )
                        StaffQuestionEditorDialog(
                            draft = sampleEditorDraft,
                            isNew = false,
                            onDraftChange = {},
                            onDismiss = {},
                            onSave = {},
                        )
                    }
                }
            }
        }
        onNodeWithText("問題を編集").assertIsDisplayed()
        captureSurfacePng("04-question-editor.png")
    }

    @OptIn(ExperimentalTestApi::class, ExperimentalMaterial3Api::class)
    @Test
    fun captureCreateFolderDialog() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizTheme {
                StaffConsolePreview(
                    selectedTab = StaffTab.Quiz,
                    shellState = sampleShellState.copy(showCreateFolderDialog = true),
                    quizContent = {
                        StaffQuizContent(
                            quizTitle = "Day 1 · Easy",
                            questions = sampleQuestions,
                            isLoading = false,
                            errorMessage = null,
                            onRefresh = {},
                            onAddQuestion = {},
                            onEditQuestion = {},
                            onRequestDeleteQuestion = {},
                            onReorderQuestions = { _, _ -> },
                        )
                    },
                )
            }
        }
        onNodeWithText("フォルダを追加").assertIsDisplayed()
        captureSurfacePng("05-create-folder.png")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@androidx.compose.runtime.Composable
private fun StaffConsolePreview(
    selectedTab: StaffTab,
    quizContent: @androidx.compose.runtime.Composable () -> Unit,
    shellState: StaffShellUiState = sampleShellState,
) {
    var newFolderName by remember { mutableStateOf("") }
    var newFolderDescription by remember { mutableStateOf("") }

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
                        TextButton(onClick = {}) { Text("ログアウト") }
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
                    onIntent = {},
                    newFolderName = newFolderName,
                    onNewFolderNameChange = { newFolderName = it },
                    newFolderDescription = newFolderDescription,
                    onNewFolderDescriptionChange = { newFolderDescription = it },
                )
                NavigationRail {
                    NavigationRailItem(
                        selected = selectedTab == StaffTab.Quiz,
                        onClick = {},
                        icon = {
                            Icon(Icons.AutoMirrored.Filled.List, contentDescription = null)
                        },
                        label = { Text("クイズ") },
                    )
                    NavigationRailItem(
                        selected = selectedTab == StaffTab.Ranking,
                        onClick = {},
                        icon = {
                            Icon(Icons.Default.EmojiEvents, contentDescription = null)
                        },
                        label = { Text("ランキング") },
                    )
                }
                Box(modifier = Modifier.weight(1f)) {
                    quizContent()
                }
            }
        }
    }
}

private val sampleShellState = StaffShellUiState(
    folders = listOf(
        QuizFolder(id = "day1-easy", name = "Day 1 · Easy", description = "会場向け初級", sortOrder = 0),
        QuizFolder(id = "day1-hard", name = "Day 1 · Hard", description = "上級者向け", sortOrder = 1),
        QuizFolder(id = "day2", name = "Day 2", description = "", sortOrder = 2),
    ),
    selectedFolderId = "day1-easy",
    activeFolderId = "day1-easy",
    isLoading = false,
)

private val sampleQuestions = listOf(
    SingleChoice(
        id = "q1",
        prompt = "DroidKaigi の開催都市は？",
        explanationMarkdown = "東京で開催されます。",
        options = listOf(
            ChoiceOption("opt1", "東京"),
            ChoiceOption("opt2", "大阪"),
            ChoiceOption("opt3", "福岡"),
        ),
        correctId = "opt1",
    ),
    MultipleChoice(
        id = "q2",
        prompt = "Compose の特徴として正しいものをすべて選べ",
        options = listOf(
            ChoiceOption("opt1", "宣言的 UI"),
            ChoiceOption("opt2", "XML 必須"),
            ChoiceOption("opt3", "状態ホイスティング"),
        ),
        correctIds = setOf("opt1", "opt3"),
    ),
    Reorder(
        id = "q3",
        prompt = "アプリ起動の流れを正しい順に並べ替えよ",
        items = listOf(
            ReorderItem("i1", "Application.onCreate"),
            ReorderItem("i2", "Activity.onCreate"),
            ReorderItem("i3", "setContent"),
        ),
        correctOrder = listOf("i1", "i2", "i3"),
    ),
)

private val sampleRanking = listOf(
    RankingEntry(nickname = "Alice", score = 320, completedAtEpochMillis = 0L),
    RankingEntry(nickname = "Bob", score = 280, completedAtEpochMillis = 0L),
    RankingEntry(nickname = "Carol", score = 250, completedAtEpochMillis = 0L),
)

private val sampleEditorDraft = StaffQuestionDraft(
    id = "q1",
    prompt = "DroidKaigi の開催都市は？",
    explanationMarkdown = "東京で開催されます。",
    type = StaffQuestionType.SingleChoice,
    items = listOf(
        StaffListItem("opt1", "東京"),
        StaffListItem("opt2", "大阪"),
        StaffListItem("opt3", "福岡"),
    ),
    correctSingleId = "opt1",
)
