package jp.co.yumemi.quiz.droidkaigi.feature.ranking.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry
import jp.co.yumemi.quiz.droidkaigi.core.ui.preview.QuizPreview
import jp.co.yumemi.quiz.droidkaigi.feature.ranking.RankingContent

@Preview(name = "ランキング（読み込み中）", showBackground = true, heightDp = 700)
@Composable
private fun RankingLoadingPreview() {
    QuizPreview {
        RankingContent(
            entries = emptyList(),
            highlightNickname = null,
            isLoading = true,
            onGoHomeClick = {},
        )
    }
}

@Preview(name = "ランキング", showBackground = true, heightDp = 700)
@Composable
private fun RankingListPreview() {
    QuizPreview {
        RankingContent(
            entries = listOf(
                RankingEntry("QuizMaster", 5, 1_700_000_000_000, totalCount = 5),
                RankingEntry("Kaigi太郎", 4, 1_700_000_100_000, totalCount = 5),
                RankingEntry("ComposeFan", 3, 1_700_000_200_000, totalCount = 5),
            ),
            highlightNickname = "Kaigi太郎",
            isLoading = false,
            onGoHomeClick = {},
        )
    }
}

@Preview(name = "ランキング（空）", showBackground = true, heightDp = 700)
@Composable
private fun RankingEmptyPreview() {
    QuizPreview {
        RankingContent(
            entries = emptyList(),
            highlightNickname = null,
            isLoading = false,
            onGoHomeClick = {},
        )
    }
}

@Preview(name = "ランキング（初回失敗）", showBackground = true, heightDp = 700)
@Composable
private fun RankingInitialErrorPreview() {
    QuizPreview {
        RankingContent(
            entries = emptyList(),
            highlightNickname = null,
            isLoading = false,
            errorMessage = "ランキングの取得に失敗しました",
            onRetryClick = {},
            onGoHomeClick = {},
        )
    }
}

@Preview(name = "ランキング（更新失敗）", showBackground = true, heightDp = 700)
@Composable
private fun RankingRefreshErrorPreview() {
    QuizPreview {
        RankingContent(
            entries = listOf(
                RankingEntry("QuizMaster", 5, 1_700_000_000_000, totalCount = 5),
                RankingEntry("Kaigi太郎", 4, 1_700_000_100_000, totalCount = 5),
            ),
            highlightNickname = "Kaigi太郎",
            isLoading = false,
            errorMessage = "ランキングの取得に失敗しました",
            onRetryClick = {},
            onGoHomeClick = {},
        )
    }
}
