package com.example.agentworkflow

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.agentworkflow.ui.screens.AgentChatScreen
import com.example.agentworkflow.ui.screens.IntentVisualizerScreen
import com.example.agentworkflow.ui.screens.ToolRegistryScreen
import com.example.agentworkflow.ui.theme.AgentWorkflowTheme
import com.example.agentworkflow.ui.theme.DarkBackground
import com.example.agentworkflow.ui.theme.PrimaryBlue
import com.example.agentworkflow.viewmodel.AgentViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    CHAT("Agent Chat", Icons.Default.Chat),
    INTENT("Intents", Icons.Default.Psychology),
    TOOLS("Tools", Icons.Default.Build)
}

class MainActivity : ComponentActivity() {

    private val viewModel: AgentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AgentWorkflowTheme {
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
fun MainAppScreen(viewModel: AgentViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.CHAT) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val serverUrl by viewModel.serverUrl.collectAsState()
    var tempUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Agent Workflow Assistant",
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
                NavigationTab.CHAT -> AgentChatScreen(viewModel)
                NavigationTab.INTENT -> IntentVisualizerScreen(viewModel)
                NavigationTab.TOOLS -> ToolRegistryScreen(viewModel)
            }
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Python Backend Configuration") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Configure FastAPI Agent Server URL (or use Local Fallback):", fontSize = 12.sp)
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
