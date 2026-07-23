package com.example.agentworkflow.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agentworkflow.data.model.IntentAnalysis
import com.example.agentworkflow.ui.theme.*
import com.example.agentworkflow.viewmodel.AgentViewModel
import com.example.agentworkflow.viewmodel.UiState

@Composable
fun IntentVisualizerScreen(viewModel: AgentViewModel) {
    val intentInput by viewModel.intentInput.collectAsState()
    val state by viewModel.intentState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Intent Recognition & Classification",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AccentPurple
            )
            Text(
                text = "Analyze how the agent parses prompt semantics, calculates intent probabilities, and extracts parameters.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // Prompt Input
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = intentInput,
                        onValueChange = { viewModel.intentInput.value = it },
                        label = { Text("Prompt for Intent Analysis") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.analyzeIntent() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Analyze Intent Probabilities", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        when (val uiState = state) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentPurple)
                    }
                }
            }
            is UiState.Error -> {
                item {
                    Text("Error: ${uiState.message}", color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val data = uiState.data

                item {
                    PrimaryIntentCard(data)
                }

                item {
                    Text("Candidate Intent Confidence Distribution", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    IntentDistributionCard(data.allScores)
                }

                item {
                    Text("Extracted Entities & Parameters", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    ExtractedParametersCard(data.extractedParameters)
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun PrimaryIntentCard(data: IntentAnalysis) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Top Class", fontSize = 12.sp, color = TextSecondary)
            Text(
                text = data.primaryIntent,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = AccentPurple
            )
            Text(
                text = "Confidence Score: ${(data.confidence * 100).toInt()}%",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = AccentEmerald
            )
        }
    }
}

@Composable
fun IntentDistributionCard(scores: Map<String, Double>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            val sorted = scores.entries.sortedByDescending { it.value }
            sorted.forEach { entry ->
                val category = entry.key
                val prob = entry.value
                val pct = (prob * 100).toInt()
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(category, fontSize = 12.sp, color = TextPrimary)
                        Text("$pct%", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
                    }
                    LinearProgressIndicator(
                        progress = { prob.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = AccentPurple,
                        trackColor = DarkSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun ExtractedParametersCard(params: Map<String, Any>) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (params.isEmpty()) {
                Text("No extra parameters required for this intent.", fontSize = 13.sp, color = TextSecondary)
            } else {
                params.forEach { (key, value) ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(key, fontSize = 13.sp, color = PrimaryBlue, fontWeight = FontWeight.Medium)
                        Text(
                            text = value.toString(),
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            color = TextPrimary
                        )
                    }
                }
            }
        }
    }
}
