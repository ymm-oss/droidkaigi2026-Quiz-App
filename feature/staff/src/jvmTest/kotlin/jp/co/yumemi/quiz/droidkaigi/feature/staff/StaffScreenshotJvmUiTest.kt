package jp.co.yumemi.quiz.droidkaigi.feature.staff

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ChoiceOption
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Reorder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.ReorderItem
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizStaffTheme
import jp.co.yumemi.quiz.droidkaigi.feature.staff.auth.StaffAuthContent
import jp.co.yumemi.quiz.droidkaigi.feature.staff.folders.StaffFolderSidebar
import jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz.StaffListItem
import jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz.StaffQuestionDraft
import jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz.StaffQuestionEditorPanel
import jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz.StaffQuestionType
import jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz.StaffQuizContent
import jp.co.yumemi.quiz.droidkaigi.feature.staff.ranking.StaffRankingContent
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
            QuizStaffTheme {
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureConsoleQuiz() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(selectedTab = StaffTab.Quiz) {
                    StaffQuizContent(
                        quizTitle = "Day 1 · Easy",
                        quizSubtitle = "会場向け初級",
                        questions = sampleQuestions,
                        isLoading = false,
                        errorMessage = null,
                        onRefresh = {},
                        onAddQuestion = {},
                        onEditQuestion = {},
                        onRequestDeleteQuestion = {},
                        onReorderQuestions = { _, _ -> },
                    )
                }
            }
        }
        onNodeWithText("問題を追加").assertIsDisplayed()
        captureSurfacePng("02-console-quiz.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureConsoleRanking() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(selectedTab = StaffTab.Ranking) {
                    StaffRankingContent(
                        entries = sampleRanking,
                        isLoading = false,
                        loadError = null,
                        onRefresh = {},
                        onRequestDeleteEntry = {},
                        onRequestClearToday = {},
                    )
                }
            }
        }
        onNodeWithText("本日のランキング").assertIsDisplayed()
        onNodeWithText("すべて削除").assertIsDisplayed()
        captureSurfacePng("03-console-ranking.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureQuestionEditor() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(selectedTab = StaffTab.Quiz) {
                    Row(modifier = Modifier.fillMaxSize()) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(),
                        ) {
                            StaffQuizContent(
                                quizTitle = "Day 1 · Easy",
                                quizSubtitle = "会場向け初級",
                                questions = sampleQuestions,
                                isLoading = false,
                                errorMessage = null,
                                onRefresh = {},
                                onAddQuestion = {},
                                onEditQuestion = {},
                                onRequestDeleteQuestion = {},
                                onReorderQuestions = { _, _ -> },
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.6f)),
                            )
                        }
                        StaffQuestionEditorPanel(
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

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureCreateFolderDialog() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(
                    selectedTab = StaffTab.Quiz,
                    shellState = sampleShellState.copy(showCreateFolderDialog = true),
                ) {
                    StaffQuizContent(
                        quizTitle = "Day 1 · Easy",
                        quizSubtitle = "会場向け初級",
                        questions = sampleQuestions,
                        isLoading = false,
                        errorMessage = null,
                        onRefresh = {},
                        onAddQuestion = {},
                        onEditQuestion = {},
                        onRequestDeleteQuestion = {},
                        onReorderQuestions = { _, _ -> },
                    )
                }
            }
        }
        onNodeWithText("フォルダを追加").assertIsDisplayed()
        captureSurfacePng("05-create-folder.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureEditFolderDialog() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(
                    selectedTab = StaffTab.Quiz,
                    shellState = sampleShellState.copy(editingFolderId = "day1-easy"),
                ) {
                    StaffQuizContent(
                        quizTitle = "Day 1 · Easy",
                        quizSubtitle = "会場向け初級",
                        questions = sampleQuestions,
                        isLoading = false,
                        errorMessage = null,
                        onRefresh = {},
                        onAddQuestion = {},
                        onEditQuestion = {},
                        onRequestDeleteQuestion = {},
                        onReorderQuestions = { _, _ -> },
                    )
                }
            }
        }
        onNodeWithText("フォルダを編集").assertIsDisplayed()
        captureSurfacePng("05b-edit-folder.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureDeleteFolderDialog() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(
                    selectedTab = StaffTab.Quiz,
                    shellState = sampleShellState.copy(deletingFolderId = "day1-easy"),
                ) {
                    StaffQuizContent(
                        quizTitle = "Day 1 · Easy",
                        quizSubtitle = "会場向け初級",
                        questions = sampleQuestions,
                        isLoading = false,
                        errorMessage = null,
                        onRefresh = {},
                        onAddQuestion = {},
                        onEditQuestion = {},
                        onRequestDeleteQuestion = {},
                        onReorderQuestions = { _, _ -> },
                    )
                }
            }
        }
        onNodeWithText("フォルダを削除").assertIsDisplayed()
        captureSurfacePng("05c-delete-folder.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun capturePublishConfirmDialog() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(selectedTab = StaffTab.Quiz) {
                    StaffQuizContent(
                        quizTitle = "Day 1 · Easy",
                        quizSubtitle = "会場向け初級",
                        questions = sampleQuestions,
                        isLoading = false,
                        errorMessage = null,
                        onRefresh = {},
                        onAddQuestion = {},
                        onEditQuestion = {},
                        onRequestDeleteQuestion = {},
                        onReorderQuestions = { _, _ -> },
                    )
                }
                StaffConfirmDialog(
                    title = "参加者向けに公開",
                    message = "「Day 1 · Easy」を参加者アプリに公開しますか？\n公開中のフォルダは切り替わります。",
                    confirmLabel = "公開",
                    onConfirm = {},
                    onDismiss = {},
                )
            }
        }
        onNodeWithText("公開").assertIsDisplayed()
        captureSurfacePng("06-publish-confirm.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureDeleteConfirmDialog() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(selectedTab = StaffTab.Quiz) {
                    StaffQuizContent(
                        quizTitle = "Day 1 · Easy",
                        quizSubtitle = "会場向け初級",
                        questions = sampleQuestions,
                        isLoading = false,
                        errorMessage = null,
                        onRefresh = {},
                        onAddQuestion = {},
                        onEditQuestion = {},
                        onRequestDeleteQuestion = {},
                        onReorderQuestions = { _, _ -> },
                    )
                }
                StaffConfirmDialog(
                    title = "問題を削除",
                    message = "「DroidKaigi の開催都市は？」を削除しますか？\nこの操作は取り消せません。",
                    confirmLabel = "削除",
                    onConfirm = {},
                    onDismiss = {},
                    destructive = true,
                )
            }
        }
        onNodeWithText("問題を削除").assertIsDisplayed()
        captureSurfacePng("07-delete-confirm.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureEmptyQuestions() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(selectedTab = StaffTab.Quiz) {
                    StaffQuizContent(
                        quizTitle = "Day 2",
                        quizSubtitle = "",
                        questions = emptyList(),
                        isLoading = false,
                        errorMessage = null,
                        onRefresh = {},
                        onAddQuestion = {},
                        onEditQuestion = {},
                        onRequestDeleteQuestion = {},
                        onReorderQuestions = { _, _ -> },
                    )
                }
            }
        }
        onNodeWithText("問題がありません").assertIsDisplayed()
        captureSurfacePng("08-empty-questions.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureNoFolderSelected() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(
                    selectedTab = StaffTab.Quiz,
                    shellState = sampleShellState.copy(selectedFolderId = null),
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        StaffNoFolderSelected(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
        onNodeWithText("フォルダを選択してください").assertIsDisplayed()
        captureSurfacePng("09-no-folder-selected.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureRankingDeleteConfirmDialog() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(selectedTab = StaffTab.Ranking) {
                    StaffRankingContent(
                        entries = sampleRanking,
                        isLoading = false,
                        loadError = null,
                        onRefresh = {},
                        onRequestDeleteEntry = {},
                        onRequestClearToday = {},
                    )
                }
                StaffConfirmDialog(
                    title = "ランキングを削除",
                    message = "「Alice」のスコアを削除しますか？\nこの操作は取り消せません。",
                    confirmLabel = "削除",
                    onConfirm = {},
                    onDismiss = {},
                    destructive = true,
                )
            }
        }
        onNodeWithText("ランキングを削除").assertIsDisplayed()
        captureSurfacePng("10-ranking-delete-confirm.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun captureRankingClearTodayConfirmDialog() = runDesktopComposeUiTest(width = 1440, height = 900) {
        setContent {
            QuizStaffTheme {
                StaffConsolePreview(selectedTab = StaffTab.Ranking) {
                    StaffRankingContent(
                        entries = sampleRanking,
                        isLoading = false,
                        loadError = null,
                        onRefresh = {},
                        onRequestDeleteEntry = {},
                        onRequestClearToday = {},
                    )
                }
                StaffConfirmDialog(
                    title = "本日のランキングをすべて削除",
                    message = "本日のランキングをすべて削除しますか？\nこの操作は取り消せません。",
                    confirmLabel = "すべて削除",
                    onConfirm = {},
                    onDismiss = {},
                    destructive = true,
                )
            }
        }
        onNodeWithText("本日のランキングをすべて削除").assertIsDisplayed()
        captureSurfacePng("11-ranking-clear-confirm.png")
    }
}

@Composable
private fun StaffConsolePreview(
    selectedTab: StaffTab,
    shellState: StaffShellUiState = sampleShellState,
    content: @Composable () -> Unit,
) {
    var newFolderName by remember { mutableStateOf("") }
    var newFolderDescription by remember { mutableStateOf("") }

    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.surface) {
        Column(modifier = Modifier.fillMaxSize()) {
            StaffTopBar(
                sitePublished = shellState.sitePublished,
                onToggleSitePublished = {},
                onSignOut = {},
            )
            Row(modifier = Modifier.fillMaxSize()) {
                StaffNavigationRail(selectedTab = selectedTab, onSelectTab = {})
                StaffFolderSidebar(
                    state = shellState,
                    onIntent = {},
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
                    content()
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
    sitePublished = false,
    isLoading = false,
)

private val sampleQuestions = listOf(
    SingleChoice(
        id = "q1",
        prompt = """
            次のコードの `Text` は **Compose Multiplatform** のコンポーネントです。

            ```kotlin
            @Composable
            fun Greeting() {
                Text("Hello, DroidKaigi!")
            }
            ```
        """.trimIndent(),
        explanationMarkdown = "DroidKaigi 2024 は東京で開催されます。",
        options = listOf(
            ChoiceOption("opt1", "東京"),
            ChoiceOption("opt2", "大阪"),
            ChoiceOption("opt3", "福岡"),
        ),
        correctId = "opt1",
    ),
    MultipleChoice(
        id = "q2",
        prompt = """
            次の **Compose** コードについて、正しい説明をすべて選んでください。

            `var count by remember { mutableStateOf(0) }`
        """.trimIndent(),
        explanationMarkdown = "UI 構築の基本原則に関する問題",
        options = listOf(
            ChoiceOption("opt1", "宣言的 UI"),
            ChoiceOption("opt2", "XML 必須"),
            ChoiceOption("opt3", "状態ホイスティング"),
        ),
        correctIds = setOf("opt1", "opt3"),
    ),
    Reorder(
        id = "q3",
        prompt = """
            # Activity 起動〜描画

            アプリ起動の流れを正しい順に並べ替えてください。
        """.trimIndent(),
        explanationMarkdown = "Android のライフサイクルと初期化プロセス",
        items = listOf(
            ReorderItem("i1", "Application.onCreate"),
            ReorderItem("i2", "Activity.onCreate"),
            ReorderItem("i3", "setContent"),
        ),
        correctOrder = listOf("i1", "i2", "i3"),
    ),
)

// 2026-07-09T14:32Z and a few minutes earlier, so the "Completed Time" column is populated.
private const val SAMPLE_COMPLETED_AT = 1_783_607_520_000L

private val sampleRanking = listOf(
    RankingEntry(nickname = "Alice", score = 320, completedAtEpochMillis = SAMPLE_COMPLETED_AT, id = "alice"),
    RankingEntry(nickname = "Bob", score = 280, completedAtEpochMillis = SAMPLE_COMPLETED_AT - 420_000L, id = "bob"),
    RankingEntry(
        nickname = "Carol",
        score = 250,
        completedAtEpochMillis = SAMPLE_COMPLETED_AT - 1_020_000L,
        id = "carol",
    ),
)

private val sampleEditorDraft = StaffQuestionDraft(
    id = "q1",
    prompt = "**DroidKaigi** の開催都市は？",
    explanationMarkdown = "東京で開催されます。`venue` は会場側の設定です。",
    type = StaffQuestionType.SingleChoice,
    items = listOf(
        StaffListItem("opt1", "東京"),
        StaffListItem("opt2", "大阪"),
        StaffListItem("opt3", "福岡"),
    ),
    correctSingleId = "opt1",
)
