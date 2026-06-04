package com.droidkaigi.quiz.core.data.auth

import com.droidkaigi.quiz.core.data.firestore.GoogleServicesLoader
import com.droidkaigi.quiz.core.domain.auth.StaffAuthException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.net.URI
import java.net.URLEncoder
import java.net.http.HttpClient
import java.net.http.HttpRequest
import java.net.http.HttpResponse
import java.nio.charset.StandardCharsets

@Serializable
private data class SignInPasswordResponse(
    val email: String? = null,
    @SerialName("displayName") val displayName: String? = null,
    @SerialName("idToken") val idToken: String,
)

@Serializable
private data class FirebaseAuthErrorResponse(
    val error: FirebaseAuthErrorBody? = null,
)

@Serializable
private data class FirebaseAuthErrorBody(
    val message: String? = null,
)

internal actual suspend fun staffSignInWithEmailPassword(email: String, password: String): StaffSignInResult =
    withContext(Dispatchers.IO) {
        val apiKey = GoogleServicesLoader.load().apiKey
        val encodedKey = URLEncoder.encode(apiKey, StandardCharsets.UTF_8)
        val body = Json.encodeToString(
            buildJsonObject {
                put("email", email)
                put("password", password)
                put("returnSecureToken", true)
            },
        )
        val request = HttpRequest.newBuilder()
            .uri(
                URI(
                    "https://identitytoolkit.googleapis.com/v1/accounts:signInWithPassword?key=$encodedKey",
                ),
            )
            .header("Content-Type", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body))
            .build()
        val response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString())
        if (response.statusCode() !in 200..299) {
            val message = runCatching {
                Json.decodeFromString<FirebaseAuthErrorResponse>(response.body()).error?.message
            }.getOrNull()
            throw StaffAuthException(
                message?.takeIf { it.isNotBlank() }
                    ?: "メールアドレスまたはパスワードが正しくありません",
            )
        }
        val parsed = Json { ignoreUnknownKeys = true }.decodeFromString<SignInPasswordResponse>(response.body())
        StaffSignInResult(
            email = parsed.email ?: email,
            displayName = parsed.displayName?.takeIf { it.isNotBlank() } ?: "スタッフ",
            idToken = parsed.idToken,
        )
    }
