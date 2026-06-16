package com.droidkaigi.quiz.feature.quiz.preview

import com.droidkaigi.quiz.core.domain.model.ChoiceOption
import com.droidkaigi.quiz.core.domain.model.MultipleChoice
import com.droidkaigi.quiz.core.domain.model.Reorder
import com.droidkaigi.quiz.core.domain.model.ReorderItem
import com.droidkaigi.quiz.core.domain.model.SingleChoice
import com.droidkaigi.quiz.feature.quiz.quiz.QuizUiState

internal object QuizPreviewFixtures {
    val singleChoiceQuestion = SingleChoice(
        id = "preview-single",
        prompt = """
            次のコードの `Text` は **Compose Multiplatform** のコンポーネントです。

            ```kotlin
            @Composable
            fun Greeting(name: String) {
                Text(text = "Hello, ${'$'}name!")
            }
            ```

            UI を共通化できる Jetpack ライブラリはどれ？
        """.trimIndent(),
        options = listOf(
            ChoiceOption("a", "Jetpack XML"),
            ChoiceOption("b", "Compose Multiplatform"),
            ChoiceOption("c", "Flutter"),
        ),
        correctId = "b",
    )

    val multipleChoiceQuestion = MultipleChoice(
        id = "preview-multiple",
        prompt = """
            次の **Compose** コードについて、正しい説明を**すべて**選んでください。

            ```kotlin
            var count by remember { mutableStateOf(0) }
            Button(onClick = { count++ }) {
                Text("Clicked ${'$'}count times")
            }
            ```
        """.trimIndent(),
        options = listOf(
            ChoiceOption("a", "count の変更で UI が再 Composition される"),
            ChoiceOption("b", "remember は必ず ViewModel で宣言する"),
            ChoiceOption("c", "Button の onClick はユーザー操作で呼ばれる"),
            ChoiceOption("d", "Text の内容は状態に連動して更新される"),
        ),
        correctIds = setOf("a", "c", "d"),
    )

    val reorderQuestion = Reorder(
        id = "preview-reorder",
        prompt = """
            ## Activity 起動〜描画

            **Android + Compose** アプリの起動から初回描画までの処理を正しい順に並べ替えてください。
        """.trimIndent(),
        items = listOf(
            ReorderItem("1", "Application.onCreate"),
            ReorderItem("2", "Activity.onCreate"),
            ReorderItem("3", "setContent / Compose"),
            ReorderItem("4", "初回 Composition"),
        ),
        correctOrder = listOf("1", "2", "3", "4"),
    )

    fun singleChoiceState(
        selectedId: String? = "b",
        showFeedback: Boolean = false,
        lastAnswerCorrect: Boolean? = null,
    ) = QuizUiState(
        prompt = singleChoiceQuestion.prompt,
        progress = "1 / 5",
        progressFraction = 0.2f,
        question = singleChoiceQuestion,
        selectedSingleId = selectedId,
        canSubmit = selectedId != null,
        showFeedback = showFeedback,
        lastAnswerCorrect = lastAnswerCorrect,
    )

    fun multipleChoiceState() = QuizUiState(
        prompt = multipleChoiceQuestion.prompt,
        progress = "2 / 5",
        progressFraction = 0.4f,
        question = multipleChoiceQuestion,
        selectedMultipleIds = setOf("a", "c", "d"),
        canSubmit = true,
    )

    fun reorderState() = QuizUiState(
        prompt = reorderQuestion.prompt,
        progress = "3 / 5",
        progressFraction = 0.6f,
        question = reorderQuestion,
        reorderIds = reorderQuestion.items.map { it.id },
        canSubmit = true,
    )
}
