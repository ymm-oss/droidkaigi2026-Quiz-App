package jp.co.yumemi.quiz.droidkaigi.preview

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import jp.co.yumemi.quiz.droidkaigi.core.ui.preview.QuizPreview
import jp.co.yumemi.quiz.droidkaigi.feature.quiz.home.HomeContent
import jp.co.yumemi.quiz.droidkaigi.navigation.Route
import jp.co.yumemi.quiz.droidkaigi.shell.QuizAdaptiveScaffold

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
