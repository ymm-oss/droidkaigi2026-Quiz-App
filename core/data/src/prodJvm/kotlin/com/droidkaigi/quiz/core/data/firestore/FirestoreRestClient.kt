package com.droidkaigi.quiz.core.data.firestore

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

internal class FirestoreRestClient(
    private val projectId: String,
    private val apiKey: String,
    private val idTokenProvider: () -> String? = { null },
) {
    private val http = HttpClient.newHttpClient()
    private val json = Json { ignoreUnknownKeys = true }
    private val documentsRoot =
        "https://firestore.googleapis.com/v1/projects/$projectId/databases/(default)/documents"

    suspend fun listDocuments(collection: String): List<Pair<String, JsonObject>> = withContext(Dispatchers.IO) {
        val response = get("$documentsRoot/$collection")
        val root = json.parseToJsonElement(response).jsonObject
        val documents = root["documents"]?.jsonArray ?: JsonArray(emptyList())
        documents.mapNotNull { element ->
            val document = element.jsonObject
            val name = document["name"]?.jsonPrimitive?.content ?: return@mapNotNull null
            val id = name.substringAfterLast('/')
            val fields = document["fields"]?.jsonObject ?: return@mapNotNull null
            id to fields
        }
    }

    suspend fun getDocument(path: String): JsonObject? = withContext(Dispatchers.IO) {
        val response = runCatching { get("$documentsRoot/$path") }.getOrNull() ?: return@withContext null
        json.parseToJsonElement(response).jsonObject["fields"]?.jsonObject
    }

    suspend fun setDocument(path: String, fields: JsonObject) = withContext(Dispatchers.IO) {
        val body = buildJsonObject { put("fields", fields) }.toString()
        patch("$documentsRoot/$path", body)
    }

    suspend fun deleteDocument(path: String) = withContext(Dispatchers.IO) {
        delete("$documentsRoot/$path")
    }

    suspend fun addDocument(parentPath: String, collectionId: String, fields: JsonObject): Unit =
        withContext(Dispatchers.IO) {
            val body = buildJsonObject { put("fields", fields) }.toString()
            post("$documentsRoot/$parentPath/$collectionId", body)
        }

    suspend fun runQuery(parentPath: String, structuredQuery: JsonObject): List<JsonObject> =
        withContext(Dispatchers.IO) {
            val parent = "projects/$projectId/databases/(default)/documents/$parentPath"
            val body = buildJsonObject {
                put("parent", parent)
                put("structuredQuery", structuredQuery)
            }.toString()
            val response = post("$documentsRoot:runQuery", body)
            val parsed = json.parseToJsonElement(response)
            val entries = when (parsed) {
                is JsonArray -> parsed
                else -> parsed.jsonObject["results"]?.jsonArray ?: JsonArray(emptyList())
            }
            entries.mapNotNull { entry ->
                entry.jsonObject["document"]?.jsonObject?.get("fields")?.jsonObject
            }
        }

    private fun get(url: String): String =
        send(applyAuth(HttpRequest.newBuilder().uri(URI(urlWithKey(url))).GET()).build())

    private fun patch(url: String, body: String): String =
        send(
            applyAuth(
                HttpRequest.newBuilder()
                    .uri(URI(urlWithKey(url)))
                    .header("Content-Type", "application/json")
                    .method("PATCH", HttpRequest.BodyPublishers.ofString(body)),
            ).build(),
        )

    private fun post(url: String, body: String): String =
        send(
            applyAuth(
                HttpRequest.newBuilder()
                    .uri(URI(urlWithKey(url)))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body)),
            ).build(),
        )

    private fun delete(url: String): String =
        send(applyAuth(HttpRequest.newBuilder().uri(URI(urlWithKey(url))).DELETE()).build())

    private fun applyAuth(builder: HttpRequest.Builder): HttpRequest.Builder {
        val token = idTokenProvider()
        if (!token.isNullOrBlank()) {
            builder.header("Authorization", "Bearer $token")
        }
        return builder
    }

    private fun send(request: HttpRequest): String {
        val response = http.send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            error("Firestore REST ${response.statusCode()}: ${response.body()}")
        }
        return response.body()
    }

    private fun urlWithKey(url: String): String {
        val encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
        val separator = if (url.contains('?')) "&" else "?"
        return "$url${separator}key=$encodedKey"
    }
}
