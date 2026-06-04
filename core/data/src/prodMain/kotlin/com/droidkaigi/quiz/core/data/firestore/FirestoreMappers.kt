package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.dto.QuizSetDto
import com.droidkaigi.quiz.core.data.dto.toDomain
import com.droidkaigi.quiz.core.domain.model.Question
import com.droidkaigi.quiz.core.domain.model.QuizFolder
import com.droidkaigi.quiz.core.domain.model.QuizSet
import com.droidkaigi.quiz.core.domain.model.RankingEntry

internal fun FolderFirestoreDocument.toQuizFolder(folderId: String): QuizFolder {
    val doc = withResolvedLabels()
    return QuizFolder(
        id = folderId,
        name = doc.name,
        description = doc.description,
        sortOrder = doc.sortOrder,
    )
}

internal fun FolderFirestoreDocument.toQuizSet(folderId: String): QuizSet {
    val doc = withResolvedLabels()
    return QuizSet(
        id = folderId,
        title = doc.title,
        questions = doc.questions.mapNotNull { it.toDomain() },
    )
}

internal fun QuizFolder.toFirestoreDocument(quizSet: QuizSet, updatedAtEpochMillis: Long): FolderFirestoreDocument =
    FolderFirestoreDocument(
        name = name,
        description = description,
        sortOrder = sortOrder,
        title = quizSet.title,
        questions = quizSet.questions.map { it.toQuestionDto() },
        updatedAtEpochMillis = updatedAtEpochMillis,
    )

internal fun RankingFirestoreDocument.toDomain(): RankingEntry = RankingEntry(
    nickname = nickname,
    score = score,
    completedAtEpochMillis = completedAtEpochMillis,
)

private fun Question.toQuestionDto(): com.droidkaigi.quiz.core.data.dto.QuestionDto = when (this) {
    is com.droidkaigi.quiz.core.domain.model.SingleChoice -> com.droidkaigi.quiz.core.data.dto.QuestionDto(
        type = "single_choice",
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown.ifBlank { null },
        options = options.map { com.droidkaigi.quiz.core.data.dto.ChoiceOptionDto(it.id, it.label) },
        correctId = correctId,
    )
    is com.droidkaigi.quiz.core.domain.model.MultipleChoice -> com.droidkaigi.quiz.core.data.dto.QuestionDto(
        type = "multiple_choice",
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown.ifBlank { null },
        options = options.map { com.droidkaigi.quiz.core.data.dto.ChoiceOptionDto(it.id, it.label) },
        correctIds = correctIds.toList(),
    )
    is com.droidkaigi.quiz.core.domain.model.Reorder -> com.droidkaigi.quiz.core.data.dto.QuestionDto(
        type = "reorder",
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown.ifBlank { null },
        items = items.map { com.droidkaigi.quiz.core.data.dto.ReorderItemDto(it.id, it.label) },
        correctOrder = correctOrder,
    )
}
