package com.example.agentworkflow.data.model

import com.google.gson.annotations.SerializedName

data class ReasoningStep(
    @SerializedName("step_type") val stepType: String, // THOUGHT, ACTION, OBSERVATION, FINAL_ANSWER
    @SerializedName("step_number") val stepNumber: Int,
    val title: String,
    val content: String,
    @SerializedName("tool_used") val toolUsed: String? = null
)

data class IntentAnalysis(
    val prompt: String,
    @SerializedName("primary_intent") val primaryIntent: String,
    val confidence: Double,
    @SerializedName("extracted_parameters") val extractedParameters: Map<String, Any>,
    @SerializedName("all_scores") val allScores: Map<String, Double>
)

data class ToolInfo(
    val name: String,
    val description: String,
    val parameters: Map<String, String>
)

data class ToolRegistryResponse(
    @SerializedName("tool_count") val toolCount: Int,
    val tools: List<ToolInfo>
)

data class AgentResponse(
    val prompt: String,
    @SerializedName("intent_analysis") val intentAnalysis: IntentAnalysis,
    @SerializedName("tool_used") val toolUsed: String,
    @SerializedName("action_status") val actionStatus: String,
    @SerializedName("reasoning_steps") val reasoningSteps: List<ReasoningStep>,
    @SerializedName("final_answer") val finalAnswer: String,
    @SerializedName("tool_details") val toolDetails: Map<String, Any>? = null
)

// Request DTOs

data class InteractRequest(
    val prompt: String
)

data class IntentRequest(
    val prompt: String
)
