package com.example.llmevaluation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.llmevaluation.ui.screens.ModelComparisonScreen
import com.example.llmevaluation.ui.screens.SingleEvalScreen
import com.example.llmevaluation.ui.screens.TaskBenchmarkScreen
import com.example.llmevaluation.ui.theme.DarkBackground
import com.example.llmevaluation.ui.theme.LLMEvaluationTheme
import com.example.llmevaluation.ui.theme.PrimaryCyan
import com.example.llmevaluation.viewmodel.EvaluationViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    EVALUATION("Metrics", Icons.Default.Assessment),
    COMPARE("Compare", Icons.Default.Compare),
    BENCHMARKS("Tasks", Icons.Default.Leaderboard)
}

class MainActivity : ComponentActivity() {

    private val viewModel: EvaluationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            LLMEvaluationTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    MainAppScreen(viewModel)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppScreen(viewModel: EvaluationViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.EVALUATION) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val serverUrl by viewModel.serverUrl.collectAsState()
    var tempUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "LLM Evaluation Study",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryCyan)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkBackground)
            )
        },
        bottomBar = {
            NavigationBar(containerColor = MaterialTheme.colorScheme.surface) {
                NavigationTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = (selectedTab == tab),
                        onClick = { selectedTab = tab },
                        icon = { Icon(tab.icon, contentDescription = tab.title) },
                        label = { Text(tab.title) },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = PrimaryCyan,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                NavigationTab.EVALUATION -> SingleEvalScreen(viewModel)
                NavigationTab.COMPARE -> ModelComparisonScreen(viewModel)
                NavigationTab.BENCHMARKS -> TaskBenchmarkScreen(viewModel)
            }
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Python Evaluation Server Configuration") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Configure FastAPI Evaluation Server URL (or use Local Fallback):", fontSize = 12.sp)
                        OutlinedTextField(
                            value = tempUrl,
                            onValueChange = { tempUrl = it },
                            label = { Text("Server Base URL") }
                        )
                    }
                },
                confirmButton = {
                    TextButton(onClick = {
                        viewModel.setServerUrl(tempUrl)
                        showSettingsDialog = false
                    }) {
                        Text("Save & Connect")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showSettingsDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}
