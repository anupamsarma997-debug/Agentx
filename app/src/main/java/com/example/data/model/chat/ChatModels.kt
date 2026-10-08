package com.example.data.model.chat

import java.util.UUID

data class ChatMessage(
    val id: String = UUID.randomUUID().toString(),
    val role: String, // "user" or "model"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val modelUsed: String? = null,
    val groundingSources: List<GroundingWebSource> = emptyList(),
    val isError: Boolean = false
)

data class GroundingWebSource(
    val title: String,
    val url: String
)

enum class ChatModelOption(
    val modelId: String,
    val displayName: String,
    val description: String,
    val badgeIcon: String
) {
    GENERAL(
        modelId = "gemini-3.5-flash",
        displayName = "General (3.5 Flash)",
        description = "Balanced intelligence & speed for everyday tasks",
        badgeIcon = "✨"
    ),
    COMPLEX(
        modelId = "gemini-3.1-pro-preview",
        displayName = "Complex (3.1 Pro)",
        description = "Advanced reasoning, scheme deep-dives & content strategy",
        badgeIcon = "🧠"
    ),
    FAST(
        modelId = "gemini-3.1-flash-lite-preview",
        displayName = "Fast / Lite (3.1 Lite)",
        description = "Ultra-fast response for quick questions & definitions",
        badgeIcon = "⚡"
    )
}

sealed class ChatResult {
    data class Success(val message: ChatMessage) : ChatResult()
    data class Error(val message: String, val throwable: Throwable? = null) : ChatResult()
    data class ConfigurationRequired(val message: String) : ChatResult()
}

sealed class ImageResult {
    data class Success(
        val imagePath: String,
        val textDescription: String? = null
    ) : ImageResult()
    data class Error(val message: String, val throwable: Throwable? = null) : ImageResult()
    data class ConfigurationRequired(val message: String) : ImageResult()
}
