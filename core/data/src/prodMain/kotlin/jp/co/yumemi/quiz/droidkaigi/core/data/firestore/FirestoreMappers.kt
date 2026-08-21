package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import jp.co.yumemi.quiz.droidkaigi.core.data.dto.QuizSetDto
import jp.co.yumemi.quiz.droidkaigi.core.data.dto.toDomain
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.Question
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizFolder
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.QuizSet
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.RankingEntry

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

internal fun RankingFirestoreDocument.toDomain(entryId: String): RankingEntry = RankingEntry(
    nickname = nickname,
    score = score,
    completedAtEpochMillis = completedAtEpochMillis,
    id = entryId,
)

private fun Question.toQuestionDto(): jp.co.yumemi.quiz.droidkaigi.core.data.dto.QuestionDto = when (this) {
    is jp.co.yumemi.quiz.droidkaigi.core.domain.model.SingleChoice -> jp.co.yumemi.quiz.droidkaigi.core.data.dto.QuestionDto(
        type = "single_choice",
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown.ifBlank { null },
        options = options.map { jp.co.yumemi.quiz.droidkaigi.core.data.dto.ChoiceOptionDto(it.id, it.label) },
        correctId = correctId,
    )
    is jp.co.yumemi.quiz.droidkaigi.core.domain.model.MultipleChoice -> jp.co.yumemi.quiz.droidkaigi.core.data.dto.QuestionDto(
        type = "multiple_choice",
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown.ifBlank { null },
        options = options.map { jp.co.yumemi.quiz.droidkaigi.core.data.dto.ChoiceOptionDto(it.id, it.label) },
        correctIds = correctIds.toList(),
    )
    is jp.co.yumemi.quiz.droidkaigi.core.domain.model.Reorder -> jp.co.yumemi.quiz.droidkaigi.core.data.dto.QuestionDto(
        type = "reorder",
        id = id,
        prompt = prompt,
        explanationMarkdown = explanationMarkdown.ifBlank { null },
        items = items.map { jp.co.yumemi.quiz.droidkaigi.core.data.dto.ReorderItemDto(it.id, it.label) },
        correctOrder = correctOrder,
    )
}
