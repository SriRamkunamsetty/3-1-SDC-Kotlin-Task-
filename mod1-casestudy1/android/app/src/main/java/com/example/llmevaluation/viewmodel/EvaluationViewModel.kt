package com.example.llmevaluation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.llmevaluation.data.model.*
import com.example.llmevaluation.data.repository.EvaluationRepository
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

class EvaluationViewModel(
    private val repository: EvaluationRepository = EvaluationRepository()
) : ViewModel() {

    // Single Evaluation State
    var generatedTextInput = MutableStateFlow("An AI agent is an autonomous software system that interprets user intent, formulates multi-step action plans, and executes tools to achieve objectives efficiently.")
    var referenceTextInput = MutableStateFlow("An AI agent is an autonomous system that interprets user intent and executes tool actions to complete tasks.")
    var selectedCategory = MutableStateFlow("Summarization")

    private val _singleEvalState = MutableStateFlow<UiState<SingleEvalResponse>>(UiState.Idle)
    val singleEvalState: StateFlow<UiState<SingleEvalResponse>> = _singleEvalState.asStateFlow()

    // Model Comparison State
    var modelAName = MutableStateFlow("Model A (GPT-4o)")
    var modelAText = MutableStateFlow("AI agents take user goals, interpret intent, and execute tools to achieve targets with high accuracy.")
    var modelBName = MutableStateFlow("Model B (Llama-3)")
    var modelBText = MutableStateFlow("Agents take goal inputs and call API tools dynamically to finish work.")
    var compareRefText = MutableStateFlow("AI Agents process input and execute tools to complete tasks.")

    private val _comparisonState = MutableStateFlow<UiState<ModelComparisonResponse>>(UiState.Idle)
    val comparisonState: StateFlow<UiState<ModelComparisonResponse>> = _comparisonState.asStateFlow()

    // Task Benchmarks State
    private val _benchmarkState = MutableStateFlow<UiState<BenchmarkTasksResponse>>(UiState.Idle)
    val benchmarkState: StateFlow<UiState<BenchmarkTasksResponse>> = _benchmarkState.asStateFlow()

    // Server Settings
    var serverUrl = MutableStateFlow("http://10.0.2.2:8000")

    init {
        evaluateSingle()
        compareModels()
        loadBenchmarkTasks()
    }

    fun setServerUrl(url: String) {
        serverUrl.value = url
        repository.updateBaseUrl(url)
    }

    fun evaluateSingle() {
        val gen = generatedTextInput.value
        val ref = referenceTextInput.value
        val cat = selectedCategory.value

        viewModelScope.launch {
            _singleEvalState.value = UiState.Loading
            try {
                val res = repository.evaluateSingle(gen, ref, cat)
                _singleEvalState.value = UiState.Success(res)
            } catch (e: Exception) {
                _singleEvalState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun compareModels() {
        val mA = modelAName.value
        val tA = modelAText.value
        val mB = modelBName.value
        val tB = modelBText.value
        val ref = compareRefText.value

        viewModelScope.launch {
            _comparisonState.value = UiState.Loading
            try {
                val res = repository.compareModels(mA, tA, mB, tB, ref)
                _comparisonState.value = UiState.Success(res)
            } catch (e: Exception) {
                _comparisonState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }

    fun loadBenchmarkTasks() {
        viewModelScope.launch {
            _benchmarkState.value = UiState.Loading
            try {
                val res = repository.listBenchmarkTasks()
                _benchmarkState.value = UiState.Success(res)
            } catch (e: Exception) {
                _benchmarkState.value = UiState.Error(e.localizedMessage ?: "Unknown error")
            }
        }
    }
}
