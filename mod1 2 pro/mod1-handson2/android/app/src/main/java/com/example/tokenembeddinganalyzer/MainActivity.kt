package com.example.tokenembeddinganalyzer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Polyline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.tokenembeddinganalyzer.ui.screens.EmbeddingAnalysisScreen
import com.example.tokenembeddinganalyzer.ui.screens.PromptComparisonScreen
import com.example.tokenembeddinganalyzer.ui.screens.TokenAnalysisScreen
import com.example.tokenembeddinganalyzer.ui.theme.DarkBackground
import com.example.tokenembeddinganalyzer.ui.theme.PrimaryBlue
import com.example.tokenembeddinganalyzer.ui.theme.TokenEmbeddingTheme
import com.example.tokenembeddinganalyzer.viewmodel.MainViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    TOKENIZER("Tokens", Icons.Default.Analytics),
    EMBEDDINGS("Embeddings", Icons.Default.Polyline),
    COMPARISON("Compare", Icons.Default.Compare)
}

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TokenEmbeddingTheme {
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
fun MainAppScreen(viewModel: MainViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.TOKENIZER) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val serverUrl by viewModel.serverUrl.collectAsState()
    var tempUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Token & Embedding Analyzer",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                actions = {
                    IconButton(onClick = { showSettingsDialog = true }) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings", tint = PrimaryBlue)
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
                            selectedIconColor = PrimaryBlue,
                            indicatorColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding)) {
            when (selectedTab) {
                NavigationTab.TOKENIZER -> TokenAnalysisScreen(viewModel)
                NavigationTab.EMBEDDINGS -> EmbeddingAnalysisScreen(viewModel)
                NavigationTab.COMPARISON -> PromptComparisonScreen(viewModel)
            }
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Python Backend Configuration") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Configure FastAPI Python Server URL (or use Local Fallback):", fontSize = 12.sp)
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
