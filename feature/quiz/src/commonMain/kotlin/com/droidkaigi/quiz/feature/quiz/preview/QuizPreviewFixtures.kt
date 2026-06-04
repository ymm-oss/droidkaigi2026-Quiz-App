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
        prompt = "Kotlin Multiplatform で UI を共通化するフレームワークは？",
        options = listOf(
            ChoiceOption("a", "Jetpack XML"),
            ChoiceOption("b", "Compose Multiplatform"),
            ChoiceOption("c", "Flutter"),
        ),
        correctId = "b",
    )

    val multipleChoiceQuestion = MultipleChoice(
        id = "preview-multiple",
        prompt = "Android アプリ開発で使われる言語をすべて選んでください",
        options = listOf(
            ChoiceOption("a", "Kotlin"),
            ChoiceOption("b", "Swift"),
            ChoiceOption("c", "Java"),
            ChoiceOption("d", "Dart"),
        ),
        correctIds = setOf("a", "c"),
    )

    val reorderQuestion = Reorder(
        id = "preview-reorder",
        prompt = "アプリ起動から画面表示までの流れを正しい順に並べてください",
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
        selectedMultipleIds = setOf("a", "c"),
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
