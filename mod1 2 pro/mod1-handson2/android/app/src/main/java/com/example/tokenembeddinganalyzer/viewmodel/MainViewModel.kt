package com.example.tokenembeddinganalyzer.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.tokenembeddinganalyzer.data.model.*
import com.example.tokenembeddinganalyzer.data.repository.AnalysisRepository
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

class MainViewModel(
    private val repository: AnalysisRepository = AnalysisRepository()
) : ViewModel() {

    // Token Analysis State
    var tokenInputText = MutableStateFlow("Hello world! How are tokens represented in LLMs?")
    var selectedEncoding = MutableStateFlow("cl100k_base")

    private val _tokenState = MutableStateFlow<UiState<TokenizationResponse>>(UiState.Idle)
    val tokenState: StateFlow<UiState<TokenizationResponse>> = _tokenState.asStateFlow()

    // Embedding Analysis State
    var prompt1Input = MutableStateFlow("Summarize the article in 3 sentences.")
    var prompt2Input = MutableStateFlow("Write a concise 3-sentence summary of the following document.")
    var prompt3Input = MutableStateFlow("Provide a detailed analysis with code examples.")

    private val _embeddingState = MutableStateFlow<UiState<EmbeddingAnalysisResponse>>(UiState.Idle)
    val embeddingState: StateFlow<UiState<EmbeddingAnalysisResponse>> = _embeddingState.asStateFlow()

    // Prompt Comparison State
    var comparePromptA = MutableStateFlow("Analyze tokenization and embeddings by processing sample text inputs.")
    var comparePromptB = MutableStateFlow("Token & Embedding Analysis: Evaluate subword tokenizers and vector similarity.")

    private val _comparisonState = MutableStateFlow<UiState<PromptComparisonResponse>>(UiState.Idle)
    val comparisonState: StateFlow<UiState<PromptComparisonResponse>> = _comparisonState.asStateFlow()

    // Settings / Server URL State
    var serverUrl = MutableStateFlow("http://10.0.2.2:8000")

    init {
        analyzeTokens()
        analyzeEmbeddings()
        comparePrompts()
    }

    fun setServerUrl(url: String) {
        serverUrl.value = url
        repository.updateBaseUrl(url)
    }

    fun analyzeTokens() {
        val text = tokenInputText.value
        val enc = selectedEncoding.value
        viewModelScope.launch {
            _tokenState.value = UiState.Loading
            try {
                val res = repository.analyzeTokens(text, enc)
                _tokenState.value = UiState.Success(res)
            } catch (e: Exception) {
                _tokenState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun analyzeEmbeddings() {
        val list = listOfNotNull(
            prompt1Input.value.ifBlank { null },
            prompt2Input.value.ifBlank { null },
            prompt3Input.value.ifBlank { null }
        )
        if (list.isEmpty()) return

        viewModelScope.launch {
            _embeddingState.value = UiState.Loading
            try {
                val res = repository.analyzeEmbeddings(list)
                _embeddingState.value = UiState.Success(res)
            } catch (e: Exception) {
                _embeddingState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun comparePrompts() {
        val pA = comparePromptA.value
        val pB = comparePromptB.value
        val enc = selectedEncoding.value

        viewModelScope.launch {
            _comparisonState.value = UiState.Loading
            try {
                val res = repository.comparePrompts(pA, pB, enc)
                _comparisonState.value = UiState.Success(res)
            } catch (e: Exception) {
                _comparisonState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
