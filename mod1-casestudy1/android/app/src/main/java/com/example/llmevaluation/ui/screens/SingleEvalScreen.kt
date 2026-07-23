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
import com.example.llmevaluation.data.model.SingleEvalResponse
import com.example.llmevaluation.ui.theme.*
import com.example.llmevaluation.viewmodel.EvaluationViewModel
import com.example.llmevaluation.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleEvalScreen(viewModel: EvaluationViewModel) {
    val genText by viewModel.generatedTextInput.collectAsState()
    val refText by viewModel.referenceTextInput.collectAsState()
    val state by viewModel.singleEvalState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Single Output Metric Evaluation",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryCyan
            )
            Text(
                text = "Quantitative metric evaluation: Perplexity, Coherence %, Accuracy (ROUGE/BLEU), and Latency.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

        // Input & Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = genText,
                        onValueChange = { viewModel.generatedTextInput.value = it },
                        label = { Text("Generated Model Output Text") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = refText,
                        onValueChange = { viewModel.referenceTextInput.value = it },
                        label = { Text("Gold Reference Text (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.evaluateSingle() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryCyan)
                    ) {
                        Text("Calculate Evaluation Metrics", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Evaluation Results
        when (val uiState = state) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = PrimaryCyan)
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
                    OverallScoreCard(data.overallQualityScore)
                }

                item {
                    MetricCardsRow(data)
                }

                item {
                    Text("Perplexity & Fluency Details", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    PerplexityDetailsCard(data)
                }

                item {
                    Text("Coherence & Structural Flow", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    CoherenceDetailsCard(data)
                }

                item {
                    Text("Accuracy & Reference Alignment (ROUGE / BLEU)", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    AccuracyDetailsCard(data)
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun OverallScoreCard(score: Double) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Overall Model Quality Index", fontSize = 12.sp, color = TextSecondary)
            Text(
                text = "$score / 100",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = when {
                    score >= 80.0 -> AccentEmerald
                    score >= 60.0 -> AccentAmber
                    else -> Color(0xFFF87171)
                }
            )
        }
    }
}

@Composable
fun MetricCardsRow(data: SingleEvalResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricBox(
            title = "Perplexity",
            value = "${data.perplexityMetrics.perplexity}",
            color = PrimaryCyan,
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            title = "Coherence",
            value = "${data.coherenceMetrics.coherenceScore}%",
            color = AccentPurple,
            modifier = Modifier.weight(1f)
        )
        MetricBox(
            title = "Accuracy",
            value = "${data.accuracyMetrics.accuracyScore}%",
            color = AccentEmerald,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun MetricBox(title: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = modifier
    ) {
        Column(modifier = Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(title, fontSize = 11.sp, color = TextSecondary)
            Text(value, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
fun PerplexityDetailsCard(data: SingleEvalResponse) {
    val p = data.perplexityMetrics
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Perplexity: ${p.perplexity} (${p.rating})", fontWeight = FontWeight.Bold, color = PrimaryCyan, fontSize = 14.sp)
            Text("Fluency Score: ${p.fluencyScore}%", fontSize = 12.sp, color = TextPrimary)
            Text("Cross Entropy: ${p.entropy}", fontSize = 12.sp, color = TextSecondary)
            Text("Word Count: ${p.wordCount} | Unique Words: ${p.uniqueWords}", fontSize = 12.sp, color = TextSecondary)
            Text("Repetition Penalty Factor: ${p.repetitionPenalty}", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun CoherenceDetailsCard(data: SingleEvalResponse) {
    val c = data.coherenceMetrics
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Coherence Score: ${c.coherenceScore}% (${c.rating})", fontWeight = FontWeight.Bold, color = AccentPurple, fontSize = 14.sp)
            Text("Sentence Count: ${c.sentenceCount} (Avg Length: ${c.avgSentenceLength} words)", fontSize = 12.sp, color = TextPrimary)
            Text("Discourse Connective Density: ${c.discourseDensity}", fontSize = 12.sp, color = TextSecondary)
            Text("Transitions Found: ${c.connectiveCount} (${c.connectiveCategories.joinToString()})", fontSize = 12.sp, color = TextSecondary)
        }
    }
}

@Composable
fun AccuracyDetailsCard(data: SingleEvalResponse) {
    val a = data.accuracyMetrics
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("Accuracy Score: ${a.accuracyScore}% (${a.rating})", fontWeight = FontWeight.Bold, color = AccentEmerald, fontSize = 14.sp)
            Text("ROUGE-L F1 Score: ${a.rougeLF1}", fontSize = 12.sp, color = TextPrimary)
            Text("BLEU Score: ${a.bleuScore}", fontSize = 12.sp, color = TextSecondary)
            Text("Exact Keyword Match: ${a.keywordMatchPct}%", fontSize = 12.sp, color = TextSecondary)
        }
    }
}
