package com.example.agentworkflow.ui.screens

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
import com.example.agentworkflow.data.model.ToolInfo
import com.example.agentworkflow.ui.theme.*
import com.example.agentworkflow.viewmodel.AgentViewModel
import com.example.agentworkflow.viewmodel.UiState

@Composable
fun ToolRegistryScreen(viewModel: AgentViewModel) {
    val state by viewModel.toolsState.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Agent Tool Registry Inspector",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = AccentEmerald
            )
            Text(
                text = "Available action tools registered with the Agent Dispatcher loop.",
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
                item {
                    Text("Registered Tools (${data.toolCount})", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }

                items(data.tools) { tool ->
                    ToolInfoCard(tool)
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun ToolInfoCard(tool: ToolInfo) {
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
                Text(
                    text = tool.name,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentEmerald
                )
                Surface(
                    color = AccentEmerald.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(6.dp)
                ) {
                    Text(
                        text = "READY",
                        color = AccentEmerald,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Text(
                text = tool.description,
                fontSize = 13.sp,
                color = TextSecondary
            )

            if (tool.parameters.isNotEmpty()) {
                Text("Parameters:", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = PrimaryBlue)
                tool.parameters.forEach { (name, desc) ->
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text("• $name:", fontSize = 12.sp, fontFamily = FontFamily.Monospace, color = TextPrimary)
                        Text(desc, fontSize = 12.sp, color = TextSecondary)
                    }
                }
            }
        }
    }
}
