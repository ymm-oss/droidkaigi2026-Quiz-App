package jp.co.yumemi.quiz.droidkaigi.core.data.firestore

import dev.gitlive.firebase.firestore.DocumentReference
import dev.gitlive.firebase.firestore.Source
import kotlinx.serialization.SerializationStrategy

/**
 * Firestore のローカル永続化は書き込みを即成功扱いにするため、
 * サーバー応答を待ってから完了とする。
 */
internal suspend fun <T : Any> DocumentReference.setAndAwaitServer(
    serializer: SerializationStrategy<T>,
    data: T,
) {
    set(serializer, data) {
        encodeDefaults = true
    }
    val snapshot = get(Source.SERVER)
    if (!snapshot.exists) {
        error("Firestore server did not confirm write to $path")
    }
}

internal suspend fun DocumentReference.awaitServerDeleted() {
    val snapshot = get(Source.SERVER)
    if (snapshot.exists) {
        error("Firestore server did not confirm delete for $path")
    }
}
