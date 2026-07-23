package com.example.llmevaluation.ui.screens

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
import com.example.llmevaluation.data.model.ModelComparisonResponse
import com.example.llmevaluation.ui.theme.*
import com.example.llmevaluation.viewmodel.EvaluationViewModel
import com.example.llmevaluation.viewmodel.UiState

@Composable
fun ModelComparisonScreen(viewModel: EvaluationViewModel) {
    val mA by viewModel.modelAName.collectAsState()
    val tA by viewModel.modelAText.collectAsState()
    val mB by viewModel.modelBName.collectAsState()
    val tB by viewModel.modelBText.collectAsState()
    val ref by viewModel.compareRefText.collectAsState()
    val state by viewModel.comparisonState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Model Benchmark Comparison",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AccentPurple
            )
            Text(
                text = "Compare Model A vs Model B outputs side-by-side across Perplexity, Coherence, and Accuracy.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // Inputs
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = mA,
                            onValueChange = { viewModel.modelAName.value = it },
                            label = { Text("Model A Name") },
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = mB,
                            onValueChange = { viewModel.modelBName.value = it },
                            label = { Text("Model B Name") },
                            modifier = Modifier.weight(1f)
                        )
                    }

                    OutlinedTextField(
                        value = tA,
                        onValueChange = { viewModel.modelAText.value = it },
                        label = { Text("Model A Output Text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = tB,
                        onValueChange = { viewModel.modelBText.value = it },
                        label = { Text("Model B Output Text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = ref,
                        onValueChange = { viewModel.compareRefText.value = it },
                        label = { Text("Gold Reference Text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.compareModels() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = AccentPurple)
                    ) {
                        Text("Compare Models", color = Color.White, fontWeight = FontWeight.Bold)
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
                    WinnerBanner(data.benchmarkWinner, data.recommendation)
                }

                item {
                    SideBySideComparisonCard(data)
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun WinnerBanner(winner: String, rec: String) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text("🏆 Benchmark Winner: $winner", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = AccentEmerald)
            Text(rec, fontSize = 13.sp, color = TextPrimary)
        }
    }
}

@Composable
fun SideBySideComparisonCard(data: ModelComparisonResponse) {
    val a = data.modelA
    val b = data.modelB

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Model A
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(a.name, fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 15.sp)
                Text("Overall: ${a.overallScore}/100", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text("Perplexity: ${a.perplexity}", fontSize = 12.sp, color = TextSecondary)
                Text("Coherence: ${a.coherence}%", fontSize = 12.sp, color = TextSecondary)
                Text("Accuracy: ${a.accuracy}%", fontSize = 12.sp, color = TextSecondary)
            }

            // Divider
            Divider(modifier = Modifier.width(1.dp).fillMaxHeight(), color = DarkSurfaceVariant)

            // Model B
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(b.name, fontWeight = FontWeight.Bold, color = AccentPurple, fontSize = 15.sp)
                Text("Overall: ${b.overallScore}/100", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                Text("Perplexity: ${b.perplexity}", fontSize = 12.sp, color = TextSecondary)
                Text("Coherence: ${b.coherence}%", fontSize = 12.sp, color = TextSecondary)
                Text("Accuracy: ${b.accuracy}%", fontSize = 12.sp, color = TextSecondary)
            }
        }
    }
}
