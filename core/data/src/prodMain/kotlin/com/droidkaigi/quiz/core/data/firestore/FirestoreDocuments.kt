package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.dto.QuestionDto
import kotlinx.serialization.Serializable

/** 一覧取得用（`questions` を含まないため Console 手投入でもデコードしやすい） */
@Serializable
data class FolderListFirestoreDocument(
    val name: String = "",
    val description: String = "",
    val sortOrder: Int = 0,
    val title: String = "",
) {
    fun withResolvedLabels(): FolderListFirestoreDocument {
        val resolvedTitle = title.ifBlank { name }
        val resolvedName = name.ifBlank { resolvedTitle }
        return copy(name = resolvedName, title = resolvedTitle)
    }

    fun toFolderFirestoreDocument(): FolderFirestoreDocument = FolderFirestoreDocument(
        name = name,
        description = description,
        sortOrder = sortOrder,
        title = title,
        questions = emptyList(),
    )
}

@Serializable
data class FolderFirestoreDocument(
    val name: String = "",
    val description: String = "",
    val sortOrder: Int = 0,
    val title: String = "",
    val questions: List<QuestionDto> = emptyList(),
    val updatedAtEpochMillis: Long? = null,
) {
    /** name / title の片方だけ入っている場合に揃える（表示名にドキュメント ID は使わない） */
    fun withResolvedLabels(): FolderFirestoreDocument {
        val resolvedTitle = title.ifBlank { name }
        val resolvedName = name.ifBlank { resolvedTitle }
        return copy(name = resolvedName, title = resolvedTitle)
    }
}

@Serializable
data class AppConfigFirestoreDocument(
    val activeFolderId: String = "",
    val updatedAtEpochMillis: Long? = null,
)

@Serializable
data class RankingFirestoreDocument(
    val nickname: String = "",
    val score: Int = 0,
    val completedAtEpochMillis: Long = 0L,
    val dateKey: String = "",
) {
    /** ルール必須フィールドが揃っているランキング行だけを UI に出す */
    fun isComplete(): Boolean =
        nickname.isNotBlank() && dateKey.isNotBlank() && score >= 0
}
