package com.example.llmevaluation.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.llmevaluation.data.model.BenchmarkTask
import com.example.llmevaluation.ui.theme.*
import com.example.llmevaluation.viewmodel.EvaluationViewModel
import com.example.llmevaluation.viewmodel.UiState

@Composable
fun TaskBenchmarkScreen(viewModel: EvaluationViewModel) {
    val state by viewModel.benchmarkState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Preset Benchmark Tasks",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald
            )
            Text(
                text = "Pre-configured text generation evaluation tasks with gold reference answers.",
                fontSize = 14.sp,
                color = TextSecondary
            )
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

                items(data.tasks) { task ->
                    BenchmarkTaskCard(task) {
                        viewModel.referenceTextInput.value = task.referenceText
                        viewModel.compareRefText.value = task.referenceText
                    }
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun BenchmarkTaskCard(task: BenchmarkTask, onSelectTask: () -> Unit) {
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
                Text(task.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryCyan)
                Surface(
                    color = AccentEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = task.category,
                        color = AccentEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(task.description, fontSize = 13.sp, color = TextSecondary)

            Text("Gold Reference Answer:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
            Surface(
                color = DarkSurfaceVariant,
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = task.referenceText,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier.padding(8.dp),
                    color = TextPrimary
                )
            }

            Button(
                onClick = onSelectTask,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentEmerald)
            ) {
                Text("Use as Gold Reference in Evaluation", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}
