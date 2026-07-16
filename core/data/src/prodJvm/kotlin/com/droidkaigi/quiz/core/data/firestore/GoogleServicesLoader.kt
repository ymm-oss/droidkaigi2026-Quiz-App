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
    val applicationId: String,
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
            applicationId = client.client_info.mobileSdkAppId,
        )
    }

    private fun resolveConfigFile(): File {
        System.getProperty("droidkaigi.firebase.config")?.let { path ->
            val file = File(path)
            if (file.isFile) return file
        }

        GoogleServicesLoader::class.java.getResourceAsStream("/google-services.json")?.use { stream ->
            val temp = File.createTempFile("google-services", ".json")
            temp.deleteOnExit()
            temp.writeBytes(stream.readBytes())
            return temp
        }

        val relativePaths = listOf(
            "androidApp/src/prod/google-services.json",
        )
        var dir: File? = File(System.getProperty("user.dir") ?: ".").canonicalFile
        while (dir != null) {
            for (relative in relativePaths) {
                val candidate = File(dir, relative)
                if (candidate.isFile) return candidate
            }
            val parent = dir.parentFile ?: break
            if (parent == dir) break
            dir = parent
        }

        error(
            "google-services.json が見つかりません。" +
                " androidApp/src/prod/google-services.json を配置するか、" +
                " -Ddroidkaigi.firebase.config=/絶対パス/google-services.json を指定してください。" +
                " (user.dir=${System.getProperty("user.dir")})",
        )
    }
}
