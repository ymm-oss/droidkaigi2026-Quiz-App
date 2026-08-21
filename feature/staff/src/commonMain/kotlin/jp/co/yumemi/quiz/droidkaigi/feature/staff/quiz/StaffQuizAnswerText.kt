package jp.co.yumemi.quiz.droidkaigi.feature.staff.quiz

import jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoice
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Question
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Reorder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice

internal fun questionTypeLabel(question: Question): String = when (question) {
    is SingleChoice -> "単一選択"
    is MultipleChoice -> "複数選択"
    is Reorder -> "並び替え"
}

internal fun correctAnswerText(question: Question): String = when (question) {
    is SingleChoice -> {
        question.options.firstOrNull { it.id == question.correctId }?.label
            ?: question.correctId
    }

    is MultipleChoice -> {
        question.correctIds
            .map { id -> question.options.firstOrNull { it.id == id }?.label ?: id }
            .joinToString(", ")
    }

    is Reorder -> {
        question.correctOrder
            .map { id -> question.items.firstOrNull { it.id == id }?.label ?: id }
            .joinToString(" → ")
    }
}
