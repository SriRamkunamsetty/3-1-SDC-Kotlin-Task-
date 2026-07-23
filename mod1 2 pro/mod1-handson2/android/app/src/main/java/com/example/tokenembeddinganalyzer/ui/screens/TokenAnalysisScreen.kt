package com.example.tokenembeddinganalyzer.ui.screens

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
import com.example.tokenembeddinganalyzer.data.model.TokenItem
import com.example.tokenembeddinganalyzer.data.model.TokenizationResponse
import com.example.tokenembeddinganalyzer.ui.theme.*
import com.example.tokenembeddinganalyzer.viewmodel.MainViewModel
import com.example.tokenembeddinganalyzer.viewmodel.UiState

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun TokenAnalysisScreen(viewModel: MainViewModel) {
    val inputText by viewModel.tokenInputText.collectAsState()
    val selectedEnc by viewModel.selectedEncoding.collectAsState()
    val tokenState by viewModel.tokenState.collectAsState()

    val encodings = listOf("cl100k_base", "o200k_base", "p50k_base", "char_level")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Tokenization Breakdown",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryBlue
            )
            Text(
                text = "Analyze how text is broken down into subwords, byte lengths, and token IDs.",
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = inputText,
                        onValueChange = { viewModel.tokenInputText.value = it },
                        label = { Text("Enter prompt / text input") },
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PrimaryBlue,
                            unfocusedBorderColor = DarkSurfaceVariant
                        )
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Tokenizer Model", fontSize = 12.sp, color = TextSecondary)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                encodings.forEach { enc ->
                                    FilterChip(
                                        selected = (enc == selectedEnc),
                                        onClick = {
                                            viewModel.selectedEncoding.value = enc
                                            viewModel.analyzeTokens()
                                        },
                                        label = { Text(enc, fontSize = 11.sp) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = PrimaryBlue,
                                            selectedLabelColor = Color.Black
                                        )
                                    )
                                }
                            }
                        }
                    }

                    Button(
                        onClick = { viewModel.analyzeTokens() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                    ) {
                        Text("Analyze Tokens", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // Results View
        when (val state = tokenState) {
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
                    TokenMetricsCards(data)
                }
                item {
                    Text("Visual Token Stream Chips", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Spacer(modifier = Modifier.height(8.dp))
                    VisualTokenChips(data.tokens)
                }
                item {
                    Text("Token Details Table", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                items(data.tokens) { token ->
                    TokenRowItem(token)
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun TokenMetricsCards(data: TokenizationResponse) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MetricBox(title = "Tokens", value = "${data.tokenCount}", color = PrimaryBlue, modifier = Modifier.weight(1f))
        MetricBox(title = "Characters", value = "${data.characterCount}", color = SecondaryPurple, modifier = Modifier.weight(1f))
        MetricBox(title = "Char/Token", value = "${data.compressionRatio}", color = AccentEmerald, modifier = Modifier.weight(1f))
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
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun VisualTokenChips(tokens: List<TokenItem>) {
    val colors = listOf(
        Color(0xFF0284C7), Color(0xFF7C3AED), Color(0xFF059669),
        Color(0xFFD97706), Color(0xFFDC2626), Color(0xFF4F46E5)
    )

    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        FlowRow(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            tokens.forEachIndexed { idx, token ->
                val bg = colors[idx % colors.size].copy(alpha = 0.25f)
                val borderCol = colors[idx % colors.size]
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bg)
                        .border(1.dp, borderCol, RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (token.text == "\n") "\\n" else token.text,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = TextPrimary
                        )
                        Text(
                            text = "[${token.id}]",
                            fontSize = 9.sp,
                            color = borderCol
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun TokenRowItem(token: TokenItem) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp).fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("#${token.index}", fontSize = 12.sp, color = TextSecondary)
                Text(
                    text = if (token.text == "\n") "\\n" else "\"${token.text}\"",
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp,
                    color = PrimaryBlue
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ID: ${token.id}", fontSize = 11.sp, color = AccentAmber)
                Text("${token.byteCount} bytes", fontSize = 11.sp, color = TextSecondary)
            }
        }
    }
}
