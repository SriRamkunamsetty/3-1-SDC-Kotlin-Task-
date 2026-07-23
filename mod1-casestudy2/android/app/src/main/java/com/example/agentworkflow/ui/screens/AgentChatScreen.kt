package com.example.agentworkflow.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agentworkflow.data.model.AgentResponse
import com.example.agentworkflow.data.model.ReasoningStep
import com.example.agentworkflow.ui.theme.*
import com.example.agentworkflow.viewmodel.AgentViewModel
import com.example.agentworkflow.viewmodel.UiState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AgentChatScreen(viewModel: AgentViewModel) {
    val promptInput by viewModel.promptInput.collectAsState()
    val agentState by viewModel.agentState.collectAsState()

    val presets = listOf(
        "Calculate 25 * 4 + 150",
        "What is an AI agent workflow?",
        "Write a Kotlin function for agent actions",
        "Summarize the ReAct agent design pattern",
        "System diagnostic status"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Agent Workflow Assistant",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Text(
                text = "Takes user prompt, interprets intent, formulates reasoning steps, and executes tools.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // Prompt input & preset chips
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = promptInput,
                        onValueChange = { viewModel.promptInput.value = it },
                        label = { Text("Enter prompt / user goal") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = DarkSurfaceVariant
                        )
                    )

                    Text("Sample Intent Prompts:", fontSize = 11.sp, color = TextSecondary)
                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        presets.forEach { preset ->
                            SuggestionChip(
                                onClick = {
                                    viewModel.promptInput.value = preset
                                    viewModel.sendPrompt(preset)
                                },
                                label = { Text(preset, fontSize = 11.sp) },
                                colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkSurfaceVariant)
                            )
                        }
                    }

                    Button(
                        onClick = { viewModel.sendPrompt() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Execute Agent Workflow", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Active Response View
        when (val state = agentState) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryBlue)
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text("Error: ${state.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val data = state.data

                item {
                    AgentMetadataHeader(data)
                }

                item {
                    Text("Agent Reasoning Trace (ReAct Workflow)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                items(data.reasoningSteps) { step ->
                    ReasoningStepCard(step)
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun AgentMetadataHeader(data: AgentResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Interpreted Intent", fontSize = 12.sp, color = TextSecondary)
                Surface(
                    color = AccentPurple.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = data.intentAnalysis.primaryIntent,
                        color = AccentPurple,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Confidence Score", fontSize = 12.sp, color = TextSecondary)
                Text(
                    text = "${(data.intentAnalysis.confidence * 100).toInt()}%",
                    color = AccentEmerald,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Dispatched Tool", fontSize = 12.sp, color = TextSecondary)
                Text(data.toolUsed, color = PrimaryBlue, fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }
        }
    }
}

@Composable
fun ReasoningStepCard(step: ReasoningStep) {
    val stepColor = when (step.stepType) {
        "THOUGHT" -> PrimaryBlue
        "ACTION" -> AccentPurple
        "OBSERVATION" -> AccentAmber
        "FINAL_ANSWER" -> AccentEmerald
        else -> TextPrimary
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(stepColor.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("${step.stepNumber}", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = stepColor)
                    }
                    Text(step.title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = stepColor)
                }
                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text(step.stepType, fontSize = 10.sp, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp), color = TextSecondary)
                }
            }

            Text(
                text = step.content,
                fontSize = 13.sp,
                fontFamily = if (step.stepType == "OBSERVATION") FontFamily.Monospace else FontFamily.Default,
                color = TextPrimary
            )
        }
    }
}
