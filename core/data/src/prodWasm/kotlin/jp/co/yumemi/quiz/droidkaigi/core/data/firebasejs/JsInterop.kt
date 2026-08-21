@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)
// js("...") 内で参照される引数を detekt は未使用と誤検知する
@file:Suppress("UnusedParameter")

package jp.co.yumemi.quiz.droidkaigi.core.data.firebasejs

/**
 * Kotlin/Wasm と Firebase JS SDK の間の値受け渡しは JSON 文字列を経由する。
 * Firestore ドキュメントは kotlinx.serialization の DTO と 1:1 の JSON 構造なので、
 * `JSON.parse` / `JSON.stringify` だけで双方向に変換できる。
 */
internal fun jsonParse(text: String): JsAny? = js("JSON.parse(text)")

internal fun jsonStringify(value: JsAny): String = js("JSON.stringify(value)")

/** FirebaseError の `code`（例: `failed-precondition`, `auth/invalid-credential`）を取り出す。 */
internal fun jsErrorCodeOrNull(value: JsAny?): String? =
    js("value && typeof value.code === 'string' ? value.code : null")

/** JS Error の `message` を取り出す（`JsException.message` が generic な場合の代替）。 */
internal fun jsErrorMessageOrNull(value: JsAny?): String? =
    js("value && typeof value.message === 'string' ? value.message : null")

internal fun <T : JsAny> JsArray<T>.toKotlinList(): List<T> = buildList {
    for (index in 0 until length) {
        add(get(index))
    }
}
