package jp.co.yumemi.quiz.droidkaigi.feature.quiz.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.LanguageSelector
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizHeroTitle
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizPrimaryButton
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizScreenBackground
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizSelectableOptionCard
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizSurfaceCard
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizTextField
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.Res
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.app_title
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_badge
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_error_empty_nickname
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_error_load_failed
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_error_no_published_folders
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_error_select_quiz_set
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_nickname
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_player_info
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_quiz_set_label
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_site_closed_button
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_site_closed_message
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_site_status_error_message
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_site_status_retry
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_start
import jp.co.yumemi.quiz.droidkaigi.core.ui.generated.resources.home_subtitle
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.AppLocalePreference
import jp.co.yumemi.quiz.droidkaigi.core.ui.locale.LocalAppLocaleController
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.quizSafeHorizontalPadding
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.quizSafeVerticalPadding
import org.jetbrains.compose.resources.stringResource

@Composable
fun HomeScreen(onStartQuiz: () -> Unit, viewModel: HomeViewModel = viewModel { HomeViewModel() }) {
    val state by viewModel.uiState.collectAsState()
    val localeController = LocalAppLocaleController.current

    LaunchedEffect(Unit) {
        viewModel.onIntent(HomeIntent.Shown)
    }

    LaunchedEffect(viewModel) {
        viewModel.events.collect { event ->
            when (event) {
                HomeEvent.NavigateToQuiz -> onStartQuiz()
            }
        }
    }

    val errorMessage = when (val error = state.error) {
        null -> null

        HomeError.EmptyNickname -> stringResource(Res.string.home_error_empty_nickname)

        HomeError.NoPublishedFolders -> stringResource(Res.string.home_error_no_published_folders)

        HomeError.NoFolderSelected -> stringResource(Res.string.home_error_select_quiz_set)

        is HomeError.LoadFailed -> error.detail?.takeIf { it.isNotBlank() }
            ?: stringResource(Res.string.home_error_load_failed)
    }

    HomeContent(
        nickname = state.nickname,
        isLoading = state.isLoading,
        sitePublished = state.sitePublished,
        siteStatusCheckFailed = state.siteStatusCheckFailed,
        publishedFolders = state.publishedFolders,
        selectedFolderId = state.selectedFolderId,
        errorMessage = errorMessage,
        localePreference = localeController.preference,
        onLocalePreferenceChange = localeController::select,
        onNicknameChange = { viewModel.onIntent(HomeIntent.NicknameChanged(it)) },
        onSelectFolder = { viewModel.onIntent(HomeIntent.SelectPublishedFolder(it)) },
        onStartClick = { viewModel.onIntent(HomeIntent.StartQuiz) },
        onRetrySiteStatusClick = { viewModel.onIntent(HomeIntent.RetrySiteStatus) },
    )
}

@Composable
fun HomeContent(
    nickname: String,
    isLoading: Boolean,
    errorMessage: String?,
    onNicknameChange: (String) -> Unit,
    onStartClick: () -> Unit,
    modifier: Modifier = Modifier,
    sitePublished: Boolean? = true,
    siteStatusCheckFailed: Boolean = false,
    publishedFolders: List<QuizFolder>? = null,
    selectedFolderId: String? = null,
    localePreference: AppLocalePreference = AppLocalePreference.System,
    onLocalePreferenceChange: (AppLocalePreference) -> Unit = {},
    onSelectFolder: (String) -> Unit = {},
    onRetrySiteStatusClick: () -> Unit = {},
) {
    val siteOpen = sitePublished == true
    QuizScreenBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                // キーボード表示時は表示領域をその上に収め、「クイズを始める」が
                // IME の裏に隠れない（タップがキーボードに吸われない）ようにする。
                .imePadding()
                .quizSafeHorizontalPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .verticalScroll(rememberScrollState())
                    .quizSafeVerticalPadding()
                    .padding(horizontal = QuizTokens.spacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingExtraLarge),
            ) {
                LanguageSelector(
                    selected = localePreference,
                    onSelect = onLocalePreferenceChange,
                )
                QuizHeroTitle(
                    title = stringResource(Res.string.app_title),
                    subtitle = stringResource(Res.string.home_subtitle),
                    badge = stringResource(Res.string.home_badge),
                )
                when {
                    // 取得失敗は「受付前」と区別してエラー + 再試行導線を出す
                    siteStatusCheckFailed -> {
                        QuizSurfaceCard {
                            Text(
                                text = stringResource(Res.string.home_site_status_error_message),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.error,
                            )
                        }
                        QuizPrimaryButton(
                            text = stringResource(Res.string.home_site_status_retry),
                            onClick = onRetrySiteStatusClick,
                        )
                    }

                    sitePublished == null -> {
                        QuizPrimaryButton(
                            text = stringResource(Res.string.home_start),
                            onClick = {},
                            enabled = false,
                            loading = true,
                        )
                    }

                    !siteOpen -> {
                        QuizSurfaceCard {
                            Text(
                                text = stringResource(Res.string.home_site_closed_message),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                        }
                        QuizPrimaryButton(
                            text = stringResource(Res.string.home_site_closed_button),
                            onClick = {},
                            enabled = false,
                        )
                    }

                    else -> {
                        QuizSurfaceCard {
                            Text(
                                text = stringResource(Res.string.home_player_info),
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface,
                            )
                            Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                            QuizTextField(
                                value = nickname,
                                onValueChange = onNicknameChange,
                                label = stringResource(Res.string.home_nickname),
                            )
                            errorMessage?.let { msg ->
                                Text(
                                    text = msg,
                                    color = MaterialTheme.colorScheme.error,
                                    style = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.padding(top = QuizTokens.spacingSmall),
                                )
                            }
                        }
                        if (publishedFolders != null && publishedFolders.size > 1) {
                            QuizSurfaceCard {
                                Text(
                                    text = stringResource(Res.string.home_quiz_set_label),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                                Column(verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingSmall)) {
                                    publishedFolders.forEach { folder ->
                                        QuizSelectableOptionCard(
                                            title = folder.displayName,
                                            subtitle = folder.description.takeIf { it.isNotBlank() },
                                            selected = folder.id == selectedFolderId,
                                            onClick = { onSelectFolder(folder.id) },
                                        )
                                    }
                                }
                            }
                        }
                        if (publishedFolders != null && publishedFolders.isEmpty()) {
                            QuizSurfaceCard {
                                Text(
                                    text = stringResource(Res.string.home_error_no_published_folders),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                        QuizPrimaryButton(
                            text = stringResource(Res.string.home_start),
                            onClick = onStartClick,
                            loading = isLoading,
                            enabled = publishedFolders == null || publishedFolders.isNotEmpty(),
                        )
                    }
                }
            }
        }
    }
}
