package com.example.uichatbot.data.local

import com.example.uichatbot.data.model.*
import java.text.SimpleDateFormat
import java.util.*

class LocalChatEngine {

    private val personas = listOf(
        BotPersona("HELPFUL_ASSISTANT", "Nova AI", "Helpful AI Assistant", "SmartToy", "You are Nova...", "Hello! I'm Nova, your AI assistant. How can I help you today?", "#38BDF8"),
        BotPersona("CODE_MENTOR", "DevBot", "Senior Code Mentor", "Code", "You are DevBot...", "Hey developer! I'm DevBot. What code question can we solve together?", "#A855F7"),
        BotPersona("CONCISE_SUMMARIZER", "BriefAI", "Concise Summarizer", "Compress", "You are BriefAI...", "BriefAI active. Send text or questions for instant bullet-point summaries.", "#34D399"),
        BotPersona("CREATIVE_WRITER", "Aria", "Creative Storyteller", "AutoAwesome", "You are Aria...", "Greetings! I'm Aria. What creative idea shall we explore today?", "#FBBF24")
    )

    fun getPersonas(): PersonasResponse = PersonasResponse(personas.size, personas)

    fun getSuggestions(personaId: String): List<String> {
        return when (personaId) {
            "CODE_MENTOR" -> listOf("Explain ViewModel state", "Kotlin Coroutines example", "Jetpack Compose Chat UI")
            "CONCISE_SUMMARIZER" -> listOf("Summarize key features", "TL;DR Architecture", "Bullet points please")
            "CREATIVE_WRITER" -> listOf("Tell me a sci-fi story", "Write an inspiring quote", "Brainstorm ideas")
            else -> listOf("Show code snippet", "Explain conversational flow", "Switch AI Persona")
        }
    }

    fun processMessage(message: String, personaId: String): ChatResponse {
        val persona = personas.find { it.id == personaId } ?: personas[0]
        val timeStr = SimpleDateFormat("HH:mm", Locale.getDefault()).format(Date())
        val msgLower = message.lowercase().trim()

        val reply = when (personaId) {
            "CODE_MENTOR" -> {
                """Here is a clean Kotlin Compose Chat snippet:

```kotlin
@Composable
fun ChatBubble(text: String, isUser: Boolean) {
    Surface(
        color = if (isUser) PrimaryCyan else DarkSurface,
        shape = RoundedCornerShape(12.dp)
    ) {
        Text(text = text, modifier = Modifier.padding(12.dp))
    }
}
```

This renders interactive chat bubbles in your Android layout."""
            }
            "CONCISE_SUMMARIZER" -> {
                "• Core Query: $message\n• Engine: BriefAI Offline Engine\n• Status: Processed cleanly."
            }
            "CREATIVE_WRITER" -> {
                "✨ What an inspiring prompt! When reflecting on '$message', fresh ideas spark like stars in the evening sky."
            }
            else -> {
                if (msgLower.contains("hello") || msgLower.contains("hi")) {
                    "Hello! I'm Nova, your offline AI assistant. How can I assist you with your Android Kotlin app today?"
                } else {
                    "I've received your prompt: \"$message\". The conversational flow state is updated and active!"
                }
            }
        }

        return ChatResponse(
            status = "success",
            persona = persona,
            userMessage = message,
            botResponse = reply,
            timestamp = timeStr,
            suggestions = getSuggestions(personaId)
        )
    }
}
