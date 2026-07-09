package com.droidkaigi.quiz.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.droidkaigi.quiz.core.ui.preview.QuizPreview
import com.droidkaigi.quiz.feature.quiz.home.HomeContent
import com.droidkaigi.quiz.navigation.Route
import com.droidkaigi.quiz.shell.QuizAdaptiveScaffold

@Preview(name = "スキャフォールド（ホーム・スマホ）", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ScaffoldPhoneHomePreview() {
    QuizPreview {
        QuizAdaptiveScaffold(
            currentRoute = Route.Home,
            onNavigate = {},
        ) {
            HomeContent(
                nickname = "Kaigi太郎",
                isLoading = false,
                errorMessage = null,
                onNicknameChange = {},
                onStartClick = {},
            )
        }
    }
}

@Preview(name = "スキャフォールド（ランキング・タブレット）", showBackground = true, widthDp = 840, heightDp = 600)
@Composable
private fun ScaffoldTabletRankingPreview() {
    QuizPreview {
        QuizAdaptiveScaffold(
            currentRoute = Route.Ranking,
            onNavigate = {},
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("ランキング画面")
            }
        }
    }
}

@Preview(name = "スキャフォールド（クイズ中・ナビ非表示）", showBackground = true, widthDp = 360, heightDp = 640)
@Composable
private fun ScaffoldQuizFlowPreview() {
    QuizPreview {
        QuizAdaptiveScaffold(
            currentRoute = Route.Quiz,
            onNavigate = {},
        ) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
            ) {
                Text("クイズ画面")
            }
        }
    }
}
