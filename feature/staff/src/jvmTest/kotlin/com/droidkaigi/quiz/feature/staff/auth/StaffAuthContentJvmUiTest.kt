package com.droidkaigi.quiz.feature.staff.auth

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runDesktopComposeUiTest
import com.droidkaigi.quiz.core.ui.theme.QuizTheme
import com.droidkaigi.quiz.feature.staff.captureSurfacePng
import kotlin.test.Test

/**
 * JVM Compose UI: staff login with Fake quick-sign-in button, plus PNG capture.
 */
class StaffAuthContentJvmUiTest {
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun staffAuth_fake_showsQuickSignInAndCapturesScreenshot() = runDesktopComposeUiTest(
        width = 960,
        height = 1200,
    ) {
        setContent {
            QuizTheme {
                StaffAuthContent(
                    email = "",
                    password = "",
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
        onNodeWithText("ログイン").assertIsDisplayed()
        onNodeWithText("デモアカウントでログイン").assertIsDisplayed()

        captureSurfacePng("staff-auth-fake-quick-login.png")
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun staffAuth_prod_hidesQuickSignInAndCapturesScreenshot() = runDesktopComposeUiTest(
        width = 960,
        height = 1200,
    ) {
        setContent {
            QuizTheme {
                StaffAuthContent(
                    email = "",
                    password = "",
                    isLoading = false,
                    errorMessage = null,
                    showQuickSignIn = false,
                    onEmailChange = {},
                    onPasswordChange = {},
                    onSignInClick = {},
                    onQuickSignInClick = {},
                )
            }
        }

        onNodeWithText("スタッフログイン").assertIsDisplayed()
        onNodeWithText("ログイン").assertIsDisplayed()
        onAllNodesWithText("デモアカウントでログイン").assertCountEquals(0)

        captureSurfacePng("staff-auth-prod-no-quick-login.png")
    }
}
