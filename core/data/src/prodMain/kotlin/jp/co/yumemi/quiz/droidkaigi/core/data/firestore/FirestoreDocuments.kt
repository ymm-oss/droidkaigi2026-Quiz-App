package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import jp.co.yumemi.quiz.droidkaigi.core.data.dto.QuestionDto
import jp.co.yumemi.quiz.droidkaigi.core.domain.model.PublishedFolderIds
import kotlinx.serialization.Serializable

/** 一覧取得用（`questions` を含まないため Console 手投入でもデコードしやすい） */
@Serializable
data class FolderListFirestoreDocument(
    val name: String = "",
    val description: String = "",
    val sortOrder: Int = 0,
    val title: String = "",
    val publicName: String = "",
    val publicDescription: String = "",
    val useInternalAsPublic: Boolean = false,
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
        publicName = publicName,
        publicDescription = publicDescription,
        useInternalAsPublic = useInternalAsPublic,
        questions = emptyList(),
    )
}

@Serializable
data class FolderFirestoreDocument(
    val name: String = "",
    val description: String = "",
    val sortOrder: Int = 0,
    val title: String = "",
    val publicName: String = "",
    val publicDescription: String = "",
    val useInternalAsPublic: Boolean = false,
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
    val publishedFolderIds: List<String> = emptyList(),
    /** When false, participant apps show Home as closed (default for new configs). */
    val sitePublished: Boolean = false,
    val updatedAtEpochMillis: Long? = null,
) {
    fun resolvedPublishedFolderIds(): List<String> =
        PublishedFolderIds.resolve(
            publishedFolderIds = publishedFolderIds,
            activeFolderId = activeFolderId,
        )

    fun withPublishedFolderIds(folderIds: List<String>, updatedAtEpochMillis: Long?): AppConfigFirestoreDocument {
        val cleaned = PublishedFolderIds.resolve(
            publishedFolderIds = folderIds,
            activeFolderId = "",
        )
        return copy(
            publishedFolderIds = cleaned,
            activeFolderId = cleaned.firstOrNull().orEmpty(),
            updatedAtEpochMillis = updatedAtEpochMillis,
        )
    }
}

/** Firestore `staffAppRelease/latest` — staff Desktop auto-update metadata. */
@Serializable
data class StaffAppReleaseFirestoreDocument(
    val version: String = "",
    val versionCode: Int = 0,
    val storagePath: String = "",
    val sha256: String = "",
    val releaseNotes: String = "",
    val publishedAtEpochMillis: Long? = null,
) {
    fun isComplete(): Boolean =
        version.isNotBlank() && versionCode > 0 && storagePath.isNotBlank() && sha256.isNotBlank()
}

@Serializable
data class RankingFirestoreDocument(
    val nickname: String = "",
    val score: Int = 0,
    val completedAtEpochMillis: Long = 0L,
    val dateKey: String = "",
    val totalCount: Int = 0,
) {
    /** ルール必須フィールドが揃っているランキング行だけを UI に出す */
    fun isComplete(): Boolean =
        nickname.isNotBlank() && dateKey.isNotBlank() && score >= 0
}
