package com.droidkaigi.quiz.feature.quiz.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import com.droidkaigi.quiz.core.ui.components.LanguageSelector
import com.droidkaigi.quiz.core.ui.components.QuizHeroTitle
import com.droidkaigi.quiz.core.ui.components.QuizPrimaryButton
import com.droidkaigi.quiz.core.ui.components.QuizScreenBackground
import com.droidkaigi.quiz.core.ui.components.QuizSurfaceCard
import com.droidkaigi.quiz.core.ui.components.QuizTextField
import com.droidkaigi.quiz.core.ui.generated.resources.Res
import com.droidkaigi.quiz.core.ui.generated.resources.app_title
import com.droidkaigi.quiz.core.ui.generated.resources.home_badge
import com.droidkaigi.quiz.core.ui.generated.resources.home_error_empty_nickname
import com.droidkaigi.quiz.core.ui.generated.resources.home_error_load_failed
import com.droidkaigi.quiz.core.ui.generated.resources.home_nickname
import com.droidkaigi.quiz.core.ui.generated.resources.home_player_info
import com.droidkaigi.quiz.core.ui.generated.resources.home_site_closed_button
import com.droidkaigi.quiz.core.ui.generated.resources.home_site_closed_message
import com.droidkaigi.quiz.core.ui.generated.resources.home_start
import com.droidkaigi.quiz.core.ui.generated.resources.home_subtitle
import com.droidkaigi.quiz.core.ui.locale.AppLocalePreference
import com.droidkaigi.quiz.core.ui.locale.LocalAppLocaleController
import com.droidkaigi.quiz.core.ui.theme.QuizTokens
import com.droidkaigi.quiz.core.ui.theme.quizSafeHorizontalPadding
import com.droidkaigi.quiz.core.ui.theme.quizSafeVerticalPadding
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

        is HomeError.LoadFailed -> error.detail?.takeIf { it.isNotBlank() }
            ?: stringResource(Res.string.home_error_load_failed)
    }

    HomeContent(
        nickname = state.nickname,
        isLoading = state.isLoading,
        sitePublished = state.sitePublished,
        errorMessage = errorMessage,
        localePreference = localeController.preference,
        onLocalePreferenceChange = localeController::select,
        onNicknameChange = { viewModel.onIntent(HomeIntent.NicknameChanged(it)) },
        onStartClick = { viewModel.onIntent(HomeIntent.StartQuiz) },
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
    localePreference: AppLocalePreference = AppLocalePreference.System,
    onLocalePreferenceChange: (AppLocalePreference) -> Unit = {},
) {
    val siteOpen = sitePublished != false
    QuizScreenBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
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
                if (!siteOpen) {
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
                        loading = sitePublished == null,
                    )
                } else {
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
                    QuizPrimaryButton(
                        text = stringResource(Res.string.home_start),
                        onClick = onStartClick,
                        loading = isLoading,
                    )
                }
            }
        }
    }
}
