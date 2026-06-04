package com.droidkaigi.quiz.feature.quiz.preview

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.droidkaigi.quiz.core.ui.preview.QuizPreview
import com.droidkaigi.quiz.feature.quiz.home.HomeContent
import com.droidkaigi.quiz.feature.quiz.quiz.QuizContent
import com.droidkaigi.quiz.feature.quiz.result.ResultContent

@Preview(name = "ホーム", showBackground = true)
@Composable
private fun HomeScreenPreview() {
    QuizPreview {
        HomeContent(
            nickname = "Kaigi太郎",
            isLoading = false,
            errorMessage = null,
            onNicknameChange = {},
            onStartClick = {},
        )
    }
}

@Preview(name = "ホーム（エラー）", showBackground = true)
@Composable
private fun HomeScreenErrorPreview() {
    QuizPreview {
        HomeContent(
            nickname = "",
            isLoading = false,
            errorMessage = "ニックネームを入力してください",
            onNicknameChange = {},
            onStartClick = {},
        )
    }
}

@Preview(name = "ホーム（読み込み中）", showBackground = true)
@Composable
private fun HomeScreenLoadingPreview() {
    QuizPreview {
        HomeContent(
            nickname = "Kaigi太郎",
            isLoading = true,
            errorMessage = null,
            onNicknameChange = {},
            onStartClick = {},
        )
    }
}

@Preview(name = "クイズ（単一選択）", showBackground = true, heightDp = 900)
@Composable
private fun QuizSingleChoicePreview() {
    QuizPreview {
        QuizContent(
            state = QuizPreviewFixtures.singleChoiceState(),
            onSelectSingle = {},
            onToggleMultiple = {},
            onMoveReorder = { _, _ -> },
            onSubmitAnswer = {},
        )
    }
}

@Preview(name = "クイズ（複数選択）", showBackground = true, heightDp = 900)
@Composable
private fun QuizMultipleChoicePreview() {
    QuizPreview {
        QuizContent(
            state = QuizPreviewFixtures.multipleChoiceState(),
            onSelectSingle = {},
            onToggleMultiple = {},
            onMoveReorder = { _, _ -> },
            onSubmitAnswer = {},
        )
    }
}

@Preview(name = "クイズ（並べ替え）", showBackground = true, heightDp = 900)
@Composable
private fun QuizReorderPreview() {
    QuizPreview {
        QuizContent(
            state = QuizPreviewFixtures.reorderState(),
            onSelectSingle = {},
            onToggleMultiple = {},
            onMoveReorder = { _, _ -> },
            onSubmitAnswer = {},
        )
    }
}

@Preview(name = "クイズ（正解フィードバック）", showBackground = true, heightDp = 900)
@Composable
private fun QuizCorrectFeedbackPreview() {
    QuizPreview {
        QuizContent(
            state = QuizPreviewFixtures.singleChoiceState(
                showFeedback = true,
                lastAnswerCorrect = true,
            ),
            onSelectSingle = {},
            onToggleMultiple = {},
            onMoveReorder = { _, _ -> },
            onSubmitAnswer = {},
        )
    }
}

@Preview(name = "クイズ（不正解フィードバック）", showBackground = true, heightDp = 900)
@Composable
private fun QuizIncorrectFeedbackPreview() {
    QuizPreview {
        QuizContent(
            state = QuizPreviewFixtures.singleChoiceState(
                selectedId = "a",
                showFeedback = true,
                lastAnswerCorrect = false,
            ),
            onSelectSingle = {},
            onToggleMultiple = {},
            onMoveReorder = { _, _ -> },
            onSubmitAnswer = {},
        )
    }
}

@Preview(name = "結果", showBackground = true)
@Composable
private fun ResultScreenPreview() {
    QuizPreview {
        ResultContent(
            nickname = "Kaigi太郎",
            correctCount = 4,
            totalCount = 5,
            targetScore = 850,
            onGoToRankingClick = {},
            animateScore = false,
        )
    }
}

@Preview(name = "結果（ダーク）", showBackground = true)
@Composable
private fun ResultScreenDarkPreview() {
    QuizPreview(darkTheme = true) {
        ResultContent(
            nickname = "Kaigi太郎",
            correctCount = 4,
            totalCount = 5,
            targetScore = 850,
            onGoToRankingClick = {},
            animateScore = false,
        )
    }
}
