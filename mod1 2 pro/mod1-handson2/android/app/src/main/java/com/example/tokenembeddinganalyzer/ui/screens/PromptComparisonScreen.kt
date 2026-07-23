package com.example.tokenembeddinganalyzer.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tokenembeddinganalyzer.data.model.PromptComparisonResponse
import com.example.tokenembeddinganalyzer.ui.theme.*
import com.example.tokenembeddinganalyzer.viewmodel.MainViewModel
import com.example.tokenembeddinganalyzer.viewmodel.UiState

@Composable
fun PromptComparisonScreen(viewModel: MainViewModel) {
    val promptA by viewModel.comparePromptA.collectAsState()
    val promptB by viewModel.comparePromptB.collectAsState()
    val state by viewModel.comparisonState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Prompt Comparison & Optimization",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald
            )
            Text(
                text = "Compare prompt variations side-by-side to optimize token efficiency and semantic precision.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // Prompt inputs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = promptA,
                        onValueChange = { viewModel.comparePromptA.value = it },
                        label = { Text("Prompt A (Baseline)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = promptB,
                        onValueChange = { viewModel.comparePromptB.value = it },
                        label = { Text("Prompt B (Optimized Variant)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.comparePrompts() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
                    ) {
                        Text("Compare Prompts", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        when (val uiState = state) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AccentEmerald)
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
                    RecommendationBanner(data.recommendation)
                }

                item {
                    ComparisonMetricsGrid(data)
                }

                item {
                    SideBySideCard(data)
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun RecommendationBanner(recText: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "💡 $recText",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = AccentAmber
            )
        }
    }
}

@Composable
fun ComparisonMetricsGrid(data: PromptComparisonResponse) {
    val m = data.metrics
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricBox(
            title = "Cosine Sim",
            value = "${m.cosineSimilarity}",
            color = if (m.cosineSimilarity >= 0.85) AccentEmerald else AccentAmber,
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            title = "Token Savings",
            value = "${m.tokenSavingsPercent}%",
            color = if (m.tokenSavingsPercent > 0) AccentEmerald else TextSecondary,
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            title = "Token Overlap",
            value = "${(m.jaccardTokenOverlap * 100).toInt()}%",
            color = PrimaryBlue,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun SideBySideCard(data: PromptComparisonResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Prompt A
            Column(modifier = Modifier.weight(1f)) {
                Text("Prompt A", fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tokens: ${data.promptA.tokenCount}", fontSize = 12.sp, color = TextPrimary)
                Text("Chars: ${data.promptA.characterCount}", fontSize = 12.sp, color = TextSecondary)
                Text("Efficiency: ${data.promptA.compressionRatio}", fontSize = 12.sp, color = TextSecondary)
            }

            // Divider
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight(), color = DarkSurfaceVariant)

            // Prompt B
            Column(modifier = Modifier.weight(1f)) {
                Text("Prompt B", fontWeight = FontWeight.Bold, color = AccentEmerald, fontSize = 14.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Tokens: ${data.promptB.tokenCount}", fontSize = 12.sp, color = TextPrimary)
                Text("Chars: ${data.promptB.characterCount}", fontSize = 12.sp, color = TextSecondary)
                Text("Efficiency: ${data.promptB.compressionRatio}", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
