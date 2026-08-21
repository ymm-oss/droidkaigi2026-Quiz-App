package jp.co.yumemi.quiz.droidkaigi.feature.staff.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizHeroTitle
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizPrimaryButton
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizScreenBackground
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizSecondaryButton
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizSurfaceCard
import jp.co.yumemi.quiz.droidkaigi.core.ui.components.QuizTextField
import jp.co.yumemi.quiz.droidkaigi.core.ui.theme.QuizTokens

@Composable
fun StaffAuthScreen(viewModel: StaffAuthViewModel = viewModel { StaffAuthViewModel() }) {
    val state by viewModel.uiState.collectAsState()

    StaffAuthContent(
        email = state.email,
        password = state.password,
        isLoading = state.isLoading,
        errorMessage = state.errorMessage,
        showQuickSignIn = state.showQuickSignIn,
        onEmailChange = { viewModel.onIntent(StaffAuthIntent.EmailChanged(it)) },
        onPasswordChange = { viewModel.onIntent(StaffAuthIntent.PasswordChanged(it)) },
        onSignInClick = { viewModel.onIntent(StaffAuthIntent.SignIn) },
        onQuickSignInClick = { viewModel.onIntent(StaffAuthIntent.QuickSignIn) },
    )
}

@Composable
fun StaffAuthContent(
    email: String,
    password: String,
    isLoading: Boolean,
    errorMessage: String?,
    showQuickSignIn: Boolean,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSignInClick: () -> Unit,
    onQuickSignInClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    QuizScreenBackground(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .safeContentPadding(),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .widthIn(max = 480.dp)
                    .padding(horizontal = QuizTokens.spacingLarge),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingExtraLarge),
            ) {
                QuizHeroTitle(
                    title = "スタッフログイン",
                    subtitle = "運営コンソールへサインイン",
                    badge = "Staff",
                )
                QuizSurfaceCard {
                    Text(
                        text = "認証情報",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    QuizTextField(
                        value = email,
                        onValueChange = onEmailChange,
                        label = "メールアドレス",
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    )
                    Spacer(modifier = Modifier.height(QuizTokens.spacingMedium))
                    QuizTextField(
                        value = password,
                        onValueChange = onPasswordChange,
                        label = "パスワード",
                        visualTransformation = PasswordVisualTransformation(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
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
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(QuizTokens.spacingMedium),
                ) {
                    QuizPrimaryButton(
                        text = "ログイン",
                        onClick = onSignInClick,
                        loading = isLoading,
                    )
                    if (showQuickSignIn) {
                        QuizSecondaryButton(
                            text = "デモアカウントでログイン",
                            onClick = onQuickSignInClick,
                            enabled = !isLoading,
                        )
                    }
                }
            }
        }
    }
}
