package com.example.tokenembeddinganalyzer.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tokenembeddinganalyzer.data.model.EmbeddingAnalysisResponse
import com.example.tokenembeddinganalyzer.ui.theme.*
import com.example.tokenembeddinganalyzer.viewmodel.MainViewModel
import com.example.tokenembeddinganalyzer.viewmodel.UiState

@Composable
fun EmbeddingAnalysisScreen(viewModel: MainViewModel) {
    val p1 by viewModel.prompt1Input.collectAsState()
    val p2 by viewModel.prompt2Input.collectAsState()
    val p3 by viewModel.prompt3Input.collectAsState()
    val state by viewModel.embeddingState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Embedding & Vector Analysis",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = SecondaryPurple
            )
            Text(
                text = "Calculate high-dimensional text embeddings, inspect activations, and measure Cosine Similarity.",
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = p1,
                        onValueChange = { viewModel.prompt1Input.value = it },
                        label = { Text("Prompt 1") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = p2,
                        onValueChange = { viewModel.prompt2Input.value = it },
                        label = { Text("Prompt 2") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = p3,
                        onValueChange = { viewModel.prompt3Input.value = it },
                        label = { Text("Prompt 3 (Optional)") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Button(
                        onClick = { viewModel.analyzeEmbeddings() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = SecondaryPurple)
                    ) {
                        Text("Calculate Embeddings & Vectors", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        when (val uiState = state) {
            is UiState.Loading -> {
                item {
                    Box(modifier = Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = SecondaryPurple)
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
                    Text("Cosine Similarity Matrix", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    SimilarityMatrixCard(data)
                }

                item {
                    Text("2D Vector Space Projection Map", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    VectorSpaceMap(data)
                }

                item {
                    Text("Top Activated Dimensions", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    TopDimensionsCard(data)
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun SimilarityMatrixCard(data: EmbeddingAnalysisResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            val matrix = data.similarityMatrix
            for (i in matrix.indices) {
                for (j in (i + 1) until matrix[i].size) {
                    val sim = matrix[i][j]
                    val pct = (sim * 100).toInt()
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Prompt ${i + 1} ↔ Prompt ${j + 1}", fontSize = 13.sp, color = TextPrimary)
                        Text(
                            text = "$sim (${pct}%)",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = when {
                                sim >= 0.85 -> AccentEmerald
                                sim >= 0.60 -> AccentAmber
                                else -> Color(0xFFF87171)
                            }
                        )
                    }
                    LinearProgressIndicator(
                        progress = { sim.toFloat().coerceIn(0f, 1f) },
                        modifier = Modifier.fillMaxWidth().height(6.dp),
                        color = when {
                            sim >= 0.85 -> AccentEmerald
                            sim >= 0.60 -> AccentAmber
                            else -> Color(0xFFF87171)
                        },
                        trackColor = DarkSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun VectorSpaceMap(data: EmbeddingAnalysisResponse) {
    val items = data.items
    val colors = listOf(PrimaryBlue, SecondaryPurple, AccentEmerald)

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth().height(200.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                
                // Draw grid axes
                drawLine(DarkSurfaceVariant, Offset(0f, cy), Offset(size.width, cy), strokeWidth = 2f)
                drawLine(DarkSurfaceVariant, Offset(cx, 0f), Offset(cx, size.height), strokeWidth = 2f)

                // Draw points
                items.forEachIndexed { idx, item ->
                    val px = cx + (item.x2d.toFloat() * (cx * 0.7f))
                    val py = cy - (item.y2d.toFloat() * (cy * 0.7f))
                    val col = colors[idx % colors.size]

                    drawCircle(col, radius = 12f, center = Offset(px, py))
                }
            }

            // Legend
            Column(modifier = Modifier.align(Alignment.TopStart)) {
                items.forEachIndexed { idx, item ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(modifier = Modifier.size(8.dp).background(colors[idx % colors.size]))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("P${idx + 1}: ${item.text.take(20)}...", fontSize = 10.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}

@Composable
fun TopDimensionsCard(data: EmbeddingAnalysisResponse) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            data.items.forEach { item ->
                Text("Prompt ${item.index + 1}: Top Dimension Weights", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    item.topDimensions.forEach { dim ->
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = DarkSurfaceVariant,
                            modifier = Modifier.padding(2.dp)
                        ) {
                            Text(
                                text = "D${dim.dimension}: ${dim.value}",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
                                color = TextPrimary
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}
