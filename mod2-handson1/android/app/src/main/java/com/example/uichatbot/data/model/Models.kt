package com.example.uichatbot.data.model

import com.google.gson.annotations.SerializedName

enum class MessageSender {
    USER, BOT
}

data class ChatMessage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val text: String,
    val sender: MessageSender,
    val timestamp: String = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault()).format(java.util.Date()),
    val personaId: String = "HELPFUL_ASSISTANT",
    val isCodeBlock: Boolean = false
)

data class BotPersona(
    val id: String,
    val name: String,
    val title: String,
    @SerializedName("avatar_icon") val avatarIcon: String,
    @SerializedName("system_prompt") val systemPrompt: String,
    val greeting: String,
    @SerializedName("color_hex") val colorHex: String
)

data class PersonasResponse(
    val count: Int,
    val personas: List<BotPersona>
)

data class SuggestionsResponse(
    @SerializedName("persona_id") val personaId: String,
    val suggestions: List<String>
)

data class ChatResponse(
    val status: String,
    val persona: BotPersona,
    @SerializedName("user_message") val userMessage: String,
    @SerializedName("bot_response") val botResponse: String,
    val timestamp: String,
    val suggestions: List<String>
)

// Request DTOs

data class ChatMessageRequest(
    val message: String,
    @SerializedName("persona_id") val personaId: String = "HELPFUL_ASSISTANT"
)
