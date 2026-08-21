package jp.co.yumemi.quiz.droidkaigi.core.data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Firestore `folders/{id}` のドキュメント ID（ランダム UUID） */
@OptIn(ExperimentalUuidApi::class)
fun newFolderDocumentId(): String = Uuid.random().toString()
