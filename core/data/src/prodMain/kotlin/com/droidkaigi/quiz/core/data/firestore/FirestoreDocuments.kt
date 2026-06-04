package com.droidkaigi.quiz.core.data.firestore

import com.droidkaigi.quiz.core.data.dto.QuestionDto
import kotlinx.serialization.Serializable

@Serializable
data class FolderFirestoreDocument(
    val name: String,
    val description: String = "",
    val sortOrder: Int = 0,
    val title: String,
    val questions: List<QuestionDto> = emptyList(),
    val updatedAtEpochMillis: Long? = null,
)

@Serializable
data class AppConfigFirestoreDocument(
    val activeFolderId: String,
    val updatedAtEpochMillis: Long? = null,
)

@Serializable
data class RankingFirestoreDocument(
    val nickname: String,
    val score: Int,
    val completedAtEpochMillis: Long,
    val dateKey: String,
)
