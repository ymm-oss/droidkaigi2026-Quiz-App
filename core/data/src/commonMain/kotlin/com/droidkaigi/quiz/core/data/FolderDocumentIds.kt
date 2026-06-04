package com.droidkaigi.quiz.core.data

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

/** Firestore `folders/{id}` のドキュメント ID（ランダム UUID） */
@OptIn(ExperimentalUuidApi::class)
fun newFolderDocumentId(): String = Uuid.random().toString()
