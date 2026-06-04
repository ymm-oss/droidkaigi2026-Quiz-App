package com.droidkaigi.quiz.core.data.firestore

import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

internal object FirestoreJsonCodec {
    private val json = Json { ignoreUnknownKeys = true }

    fun <T> encode(value: T, serializer: KSerializer<T>): JsonObject =
        json.encodeToJsonElement(serializer, value).toFirestoreFields()

    fun <T> decode(fields: JsonObject?, serializer: KSerializer<T>): T? {
        if (fields == null) return null
        return json.decodeFromJsonElement(serializer, fields.toJsonElement())
    }

    private fun JsonElement.toFirestoreFields(): JsonObject = when (this) {
        is JsonObject -> buildJsonObject {
            for ((key, value) in this@toFirestoreFields) {
                put(key, value.toFirestoreValue())
            }
        }
        else -> error("Expected JsonObject at root")
    }

    private fun JsonElement.toFirestoreValue(): JsonObject = when (this) {
        is JsonObject -> buildJsonObject { put("mapValue", buildJsonObject { put("fields", this@toFirestoreValue.toFirestoreFields()) }) }
        is JsonArray -> buildJsonObject {
            put(
                "arrayValue",
                buildJsonObject {
                    put(
                        "values",
                        JsonArray(map { it.toFirestoreValue() }),
                    )
                },
            )
        }
        is JsonNull -> buildJsonObject { put("nullValue", JsonNull) }
        is JsonPrimitive -> when {
            isString -> buildJsonObject { put("stringValue", this@toFirestoreValue) }
            booleanOrNull != null -> buildJsonObject { put("booleanValue", JsonPrimitive(booleanOrNull!!)) }
            longOrNull != null -> buildJsonObject { put("integerValue", JsonPrimitive(longOrNull!!.toString())) }
            else -> buildJsonObject { put("doubleValue", JsonPrimitive(content.toDouble())) }
        }
    }

    private fun JsonObject.toJsonElement(): JsonElement {
        val mapValue = this["mapValue"]?.jsonObject?.get("fields")?.jsonObject
        if (mapValue != null) return mapValue.toJsonObjectElement()

        val arrayValue = this["arrayValue"]?.jsonObject?.get("values")
        if (arrayValue is JsonArray) {
            return JsonArray(arrayValue.map { it.jsonObject.toJsonElement() })
        }

        this["stringValue"]?.jsonPrimitive?.content?.let { return JsonPrimitive(it) }
        this["integerValue"]?.jsonPrimitive?.content?.let { return JsonPrimitive(it.toLong()) }
        this["doubleValue"]?.jsonPrimitive?.content?.let { return JsonPrimitive(it) }
        this["booleanValue"]?.jsonPrimitive?.content?.let { return JsonPrimitive(it) }
        if (containsKey("nullValue")) return JsonNull

        return JsonObject(emptyMap())
    }

    private fun JsonObject.toJsonObjectElement(): JsonObject {
        val result = mutableMapOf<String, JsonElement>()
        for ((key, value) in this) {
            result[key] = value.jsonObject.toJsonElement()
        }
        return JsonObject(result)
    }
}
