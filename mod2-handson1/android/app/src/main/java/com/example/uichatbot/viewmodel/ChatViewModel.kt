package com.example.uichatbot.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.uichatbot.data.model.*
import com.example.uichatbot.data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface UiState<out T> {
    object Idle : UiState<Nothing>
    object Loading : UiState<Nothing>
    data class Success<out T>(val data: T) : UiState<T>
    data class Error(val message: String) : UiState<Nothing>
}

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {

    // Message Input
    var inputText = MutableStateFlow("")

    // Message History List
    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    // Typing State (true when bot is typing)
    private val _isBotTyping = MutableStateFlow(false)
    val isBotTyping: StateFlow<Boolean> = _isBotTyping.asStateFlow()

    // Personas State
    private val _personasState = MutableStateFlow<UiState<PersonasResponse>>(UiState.Idle)
    val personasState: StateFlow<UiState<PersonasResponse>> = _personasState.asStateFlow()

    // Active Persona
    var activePersona = MutableStateFlow(
        BotPersona("HELPFUL_ASSISTANT", "Nova AI", "Helpful AI Assistant", "SmartToy", "You are Nova...", "Hello! I'm Nova, your AI assistant. How can I help you today?", "#38BDF8")
    )

    // Suggestion Chips
    private val _suggestions = MutableStateFlow<List<String>>(emptyList())
    val suggestions: StateFlow<List<String>> = _suggestions.asStateFlow()

    // Server Settings
    var serverUrl = MutableStateFlow("http://10.0.2.2:8000")

    init {
        loadPersonas()
        loadInitialGreeting()
    }

    fun setServerUrl(url: String) {
        serverUrl.value = url
        repository.updateBaseUrl(url)
    }

    private fun loadInitialGreeting() {
        val p = activePersona.value
        _messages.value = listOf(
            ChatMessage(
                text = p.greeting,
                sender = MessageSender.BOT,
                personaId = p.id
            )
        )
        loadSuggestions(p.id)
    }

    fun selectPersona(persona: BotPersona) {
        activePersona.value = persona
        _messages.value = _messages.value + ChatMessage(
            text = "Switched to ${persona.name} (${persona.title}). ${persona.greeting}",
            sender = MessageSender.BOT,
            personaId = persona.id
        )
        loadSuggestions(persona.id)
    }

    fun loadSuggestions(personaId: String) {
        viewModelScope.launch {
            try {
                val list = repository.getSuggestions(personaId)
                _suggestions.value = list
            } catch (e: Exception) {
                _suggestions.value = listOf("Show code snippet", "Explain architecture", "Switch AI Persona")
            }
        }
    }

    fun loadPersonas() {
        viewModelScope.launch {
            _personasState.value = UiState.Loading
            try {
                val res = repository.listPersonas()
                _personasState.value = UiState.Success(res)
            } catch (e: Exception) {
                _personasState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun sendMessage(customMsg: String? = null) {
        val msg = customMsg ?: inputText.value.trim()
        if (msg.isBlank()) return

        if (customMsg == null) {
            inputText.value = ""
        }

        val userMsgObj = ChatMessage(text = msg, sender = MessageSender.USER)
        _messages.value = _messages.value + userMsgObj
        _isBotTyping.value = true

        val personaId = activePersona.value.id

        viewModelScope.launch {
            // Typing delay for realistic conversational feel
            delay(800)
            try {
                val res = repository.sendMessage(msg, personaId)
                val botMsgObj = ChatMessage(
                    text = res.botResponse,
                    sender = MessageSender.BOT,
                    timestamp = res.timestamp,
                    personaId = personaId,
                    isCodeBlock = res.botResponse.contains("```")
                )
                _messages.value = _messages.value + botMsgObj
                _suggestions.value = res.suggestions
            } catch (e: Exception) {
                val errorMsgObj = ChatMessage(
                    text = "Sorry, I couldn't process your request. Please try again.",
                    sender = MessageSender.BOT,
                    personaId = personaId
                )
                _messages.value = _messages.value + errorMsgObj
            } finally {
                _isBotTyping.value = false
            }
        }
    }

    fun clearChat() {
        loadInitialGreeting()
    }
}
