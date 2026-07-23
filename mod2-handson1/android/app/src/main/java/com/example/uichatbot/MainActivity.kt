package com.example.uichatbot

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uichatbot.ui.screens.ChatScreen
import com.example.uichatbot.ui.screens.PersonaScreen
import com.example.uichatbot.ui.theme.DarkBackground
import com.example.uichatbot.ui.theme.PrimaryCyan
import com.example.uichatbot.ui.theme.UIChatbotTheme
import com.example.uichatbot.viewmodel.ChatViewModel

enum class NavigationTab(val title: String, val icon: ImageVector) {
    CHAT("AI Chat", Icons.Default.ChatBubble),
    PERSONAS("Personas", Icons.Default.Psychology)
}

class MainActivity : ComponentActivity() {

    private val viewModel: ChatViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UIChatbotTheme {
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
fun MainAppScreen(viewModel: ChatViewModel) {
    var selectedTab by remember { mutableStateOf(NavigationTab.CHAT) }
    var showSettingsDialog by remember { mutableStateOf(false) }

    val serverUrl by viewModel.serverUrl.collectAsState()
    var tempUrl by remember(serverUrl) { mutableStateOf(serverUrl) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "UI Chatbot Interface",
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
                NavigationTab.CHAT -> ChatScreen(
                    viewModel = viewModel,
                    onNavigateToPersonas = { selectedTab = NavigationTab.PERSONAS }
                )
                NavigationTab.PERSONAS -> PersonaScreen(
                    viewModel = viewModel,
                    onPersonaSelected = { selectedTab = NavigationTab.CHAT }
                )
            }
        }

        if (showSettingsDialog) {
            AlertDialog(
                onDismissRequest = { showSettingsDialog = false },
                title = { Text("Python Chatbot Server Configuration") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Configure FastAPI Chat Server URL (or use Local Fallback):", fontSize = 12.sp)
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
