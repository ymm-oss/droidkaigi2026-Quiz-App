package com.droidkaigi.quiz.core.data.firestore

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import java.io.File

@Serializable
private data class GoogleServicesRoot(
    val project_info: ProjectInfo,
    val client: List<ClientEntry> = emptyList(),
)

@Serializable
private data class ProjectInfo(
    @SerialName("project_id") val projectId: String,
)

@Serializable
private data class ClientEntry(
    val client_info: ClientInfo,
    val api_key: List<ApiKeyEntry> = emptyList(),
)

@Serializable
private data class ClientInfo(
    @SerialName("mobilesdk_app_id") val mobileSdkAppId: String,
)

@Serializable
private data class ApiKeyEntry(
    @SerialName("current_key") val currentKey: String,
)

internal data class FirebaseProjectConfig(
    val projectId: String,
    val apiKey: String,
)

internal object GoogleServicesLoader {
    private val json = Json { ignoreUnknownKeys = true }

    fun load(): FirebaseProjectConfig {
        val text = resolveConfigFile().readText()
        val root = json.decodeFromString<GoogleServicesRoot>(text)
        val client = root.client.firstOrNull()
            ?: error("google-services.json に client が見つかりません")
        val apiKey = client.api_key.firstOrNull()?.currentKey
            ?: error("google-services.json に api_key が見つかりません")
        return FirebaseProjectConfig(
            projectId = root.project_info.projectId,
            apiKey = apiKey,
        )
    }

    private fun resolveConfigFile(): File {
        val candidates = listOfNotNull(
            System.getProperty("droidkaigi.firebase.config")?.let { File(it) },
            File("google-services.json"),
            File("androidApp/google-services.json"),
        )
        return candidates.firstOrNull { it.isFile }
            ?: error(
                "google-services.json が見つかりません。次のいずれかに配置してください: " +
                    candidates.joinToString { it.path },
            )
    }
}
