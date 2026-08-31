package jp.co.yumemi.quiz.droidkaigi.core.domain.model

import kotlinx.serialization.Serializable

/** Groups a quiz set and its rankings (e.g. by day or difficulty). */
@Serializable
data class QuizFolder(
    val id: String,
    val name: String,
    val description: String = "",
    val sortOrder: Int = 0,
    val publicName: String = "",
    val publicDescription: String = "",
    val useInternalAsPublic: Boolean = false,
) {
    /** サイドバー等の表示用（Firestore ドキュメント ID は出さない） */
    val displayName: String
        get() = name.ifBlank { "（無題）" }

    /** 参加者向け表示名。公開情報が未設定の旧データは管理名へフォールバックする。 */
    val listingName: String
        get() = if (useInternalAsPublic) displayName else publicName.ifBlank { displayName }

    /** 参加者向け説明。管理情報を使う場合のみ、管理用の説明へ追従する。 */
    val listingDescription: String
        get() = if (useInternalAsPublic) description else publicDescription
}
