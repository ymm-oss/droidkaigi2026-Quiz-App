package com.droidkaigi.quiz.feature.ranking.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.droidkaigi.quiz.core.domain.model.RankingEntry
import com.droidkaigi.quiz.core.ui.preview.QuizPreview
import com.droidkaigi.quiz.feature.ranking.RankingContent

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
                RankingEntry("QuizMaster", 980, 1_700_000_000_000),
                RankingEntry("Kaigi太郎", 850, 1_700_000_100_000),
                RankingEntry("ComposeFan", 720, 1_700_000_200_000),
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
