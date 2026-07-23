package com.example.agentworkflow.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.agentworkflow.data.model.*
import com.example.agentworkflow.data.repository.AgentRepository
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

class AgentViewModel(
    private val repository: AgentRepository = AgentRepository()
) : ViewModel() {

    // Chat Prompt State
    var promptInput = MutableStateFlow("Calculate 25 * 4 + 150")

    private val _agentState = MutableStateFlow<UiState<AgentResponse>>(UiState.Idle)
    val agentState: StateFlow<UiState<AgentResponse>> = _agentState.asStateFlow()

    // History of interactions
    private val _history = MutableStateFlow<List<AgentResponse>>(emptyList())
    val history: StateFlow<List<AgentResponse>> = _history.asStateFlow()

    // Intent Visualizer State
    var intentInput = MutableStateFlow("Write a Kotlin function to format dates")
    private val _intentState = MutableStateFlow<UiState<IntentAnalysis>>(UiState.Idle)
    val intentState: StateFlow<UiState<IntentAnalysis>> = _intentState.asStateFlow()

    // Tool Registry State
    private val _toolsState = MutableStateFlow<UiState<ToolRegistryResponse>>(UiState.Idle)
    val toolsState: StateFlow<UiState<ToolRegistryResponse>> = _toolsState.asStateFlow()

    // Server Settings
    var serverUrl = MutableStateFlow("http://10.0.2.2:8000")

    init {
        sendPrompt()
        analyzeIntent()
        loadTools()
    }

    fun setServerUrl(url: String) {
        serverUrl.value = url
        repository.updateBaseUrl(url)
    }

    fun sendPrompt(customPrompt: String? = null) {
        val prompt = customPrompt ?: promptInput.value
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _agentState.value = UiState.Loading
            try {
                val res = repository.interact(prompt)
                _agentState.value = UiState.Success(res)
                _history.value = _history.value + res
            } catch (e: Exception) {
                _agentState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun analyzeIntent() {
        val prompt = intentInput.value
        if (prompt.isBlank()) return

        viewModelScope.launch {
            _intentState.value = UiState.Loading
            try {
                val res = repository.analyzeIntent(prompt)
                _intentState.value = UiState.Success(res)
            } catch (e: Exception) {
                _intentState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loadTools() {
        viewModelScope.launch {
            _toolsState.value = UiState.Loading
            try {
                val res = repository.listTools()
                _toolsState.value = UiState.Success(res)
            } catch (e: Exception) {
                _toolsState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
