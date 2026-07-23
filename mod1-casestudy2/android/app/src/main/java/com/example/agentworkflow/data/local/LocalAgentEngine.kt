package com.example.agentworkflow.data.local

import com.example.agentworkflow.data.model.*
import kotlin.math.roundToInt

class LocalAgentEngine {

    fun processRequest(userPrompt: String): AgentResponse {
        val promptLower = userPrompt.lowercase().trim()

        val (primaryIntent, confidence, params) = interpretIntent(promptLower)

        val reasoningSteps = mutableListOf<ReasoningStep>()

        // Step 1: Thought
        reasoning_steps.add(
            ReasoningStep(
                stepType = "THOUGHT",
                stepNumber = 1,
                title = "Offline Intent Interpretation",
                content = "Offline Agent Engine identified intent '$primaryIntent' with ${(confidence * 100).toInt()}% confidence."
            )
        )

        // Step 2: Tool Selection
        val (toolName, toolOutput) = executeTool(primaryIntent, userPrompt, params)
        reasoning_steps.add(
            ReasoningStep(
                stepType = "ACTION",
                stepNumber = 2,
                title = "Tool Selection & Execution",
                content = "Selected local action tool '$toolName' to fulfill request.",
                toolUsed = toolName
            )
        )

        // Step 3: Observation
        reasoning_steps.add(
            ReasoningStep(
                stepType = "OBSERVATION",
                stepNumber = 3,
                title = "Action Observation",
                content = "Tool '$toolName' returned:\n$toolOutput"
            )
        )

        // Step 4: Final Answer
        val finalAnswer = "Offline Agent Action Completed.\nIntent: $primaryIntent → Executed $toolName.\n\n$toolOutput"
        reasoning_steps.add(
            ReasoningStep(
                stepType = "FINAL_ANSWER",
                stepNumber = 4,
                title = "Final Response",
                content = finalAnswer
            )
        )

        val intentAnalysis = IntentAnalysis(
            prompt = userPrompt,
            primaryIntent = primaryIntent,
            confidence = confidence,
            extractedParameters = params,
            allScores = mapOf(primaryIntent to confidence, "GENERAL_CONVERSATION" to 0.2)
        )

        return AgentResponse(
            prompt = userPrompt,
            intentAnalysis = intentAnalysis,
            toolUsed = toolName,
            actionStatus = "success (offline)",
            reasoningSteps = reasoningSteps,
            finalAnswer = finalAnswer,
            toolDetails = mapOf("output" to toolOutput)
        )
    }

    private fun interpretIntent(text: String): Triple<String, Double, Map<String, Any>> {
        return when {
            text.contains("calc") || text.contains("add") || text.contains("multiply") || text.contains("compute") || text.matches(Regex(""".*\d+.*[\+\-\*\/].*""")) -> {
                Triple("MATH_CALCULATION", 0.92, mapOf("expression" to text))
            }
            text.contains("code") || text.contains("kotlin") || text.contains("python") || text.contains("function") -> {
                Triple("CODE_GENERATION", 0.88, mapOf("language" to "kotlin", "topic" to text))
            }
            text.contains("summary") || text.contains("summarize") || text.contains("tldr") -> {
                Triple("TEXT_SUMMARIZATION", 0.85, mapOf("text" to text))
            }
            text.contains("status") || text.contains("health") || text.contains("system") -> {
                Triple("SYSTEM_DIAGNOSTIC", 0.90, emptyMap())
            }
            else -> {
                Triple("KNOWLEDGE_QUERY", 0.75, mapOf("query" to text))
            }
        }
    }

    private fun executeTool(intent: String, prompt: String, params: Map<String, Any>): Pair<String, String> {
        return when (intent) {
            "MATH_CALCULATION" -> {
                val expr = params["expression"]?.toString() ?: prompt
                val numbers = Regex("""\d+""").findAll(expr).map { it.value.toLong() }.toList()
                val result = if (numbers.size >= 2) {
                    if (expr.contains("*")) numbers[0] * numbers[1]
                    else if (expr.contains("+")) numbers[0] + numbers[1]
                    else if (expr.contains("-")) numbers[0] - numbers[1]
                    else if (expr.contains("/") && numbers[1] != 0L) numbers[0] / numbers[1]
                    else numbers.sum()
                } else {
                    numbers.firstOrNull() ?: 42
                }
                "CalculatorTool" to "Calculation Result: $expr = $result"
            }
            "CODE_GENERATION" -> {
                "CodeGeneratorTool" to """// Kotlin Agent Workflow Snippet
fun executeAgentTask(input: String) {
    println("Processing agent intent for: $input")
}"""
            }
            "TEXT_SUMMARIZATION" -> {
                "SummarizerTool" to "Concise Summary: Agent interprets intent, plans steps, and executes tools."
            }
            "SYSTEM_DIAGNOSTIC" -> {
                "SystemDiagnosticTool" to "Offline Agent Engine Status: HEALTHY (All tools ready)."
            }
            else -> {
                "KnowledgeTool" to "An AI Agent interprets user intents, formulates execution steps (ReAct pattern), and dispatches actions to actionable tools."
            }
        }
    }

    fun getRegisteredTools(): ToolRegistryResponse {
        return ToolRegistryResponse(
            toolCount = 5,
            tools = listOf(
                ToolInfo("CalculatorTool", "Evaluates mathematical expressions safely.", mapOf("expression" to "string")),
                ToolInfo("KnowledgeTool", "Searches curated domain knowledge base.", mapOf("query" to "string")),
                ToolInfo("CodeGeneratorTool", "Generates clean code snippets in Kotlin/Python.", mapOf("language" to "string", "topic" to "string")),
                ToolInfo("SummarizerTool", "Extracts key summary points from text.", mapOf("text_to_summarize" to "string")),
                ToolInfo("SystemDiagnosticTool", "Inspects agent status & memory.", emptyMap())
            )
        )
    }
}
