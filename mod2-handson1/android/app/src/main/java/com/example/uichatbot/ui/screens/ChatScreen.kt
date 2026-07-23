package com.example.uichatbot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uichatbot.ui.components.ChatBubble
import com.example.uichatbot.ui.components.TypingIndicator
import com.example.uichatbot.ui.theme.*
import com.example.uichatbot.viewmodel.ChatViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel, onNavigateToPersonas: () -> Unit) {
    val messages by viewModel.messages.collectAsState()
    val isBotTyping by viewModel.isBotTyping.collectAsState()
    val inputText by viewModel.inputText.collectAsState()
    val activePersona by viewModel.activePersona.collectAsState()
    val suggestions by viewModel.suggestions.collectAsState()

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to latest message on update
    LaunchedEffect(messages.size, isBotTyping) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Active Persona Header Bar
        Card(
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            shape = RoundedCornerShape(0.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(activePersona.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = PrimaryCyan)
                        Surface(
                            color = PrimaryCyan.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                text = "ONLINE",
                                color = PrimaryCyan,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                            )
                        }
                    }
                    Text(activePersona.title, fontSize = 12.sp, color = TextSecondary)
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear Chat", tint = TextSecondary)
                    }
                    Button(
                        onClick = onNavigateToPersonas,
                        colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceVariant),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                    ) {
                        Text("Personas", fontSize = 11.sp, color = TextPrimary)
                    }
                }
            }
        }

        // Messages Scroll Area
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(messages) { message ->
                ChatBubble(message)
            }

            if (isBotTyping) {
                item {
                    TypingIndicator(activePersona.name)
                }
            }
        }

        // Suggestion Chips
        if (suggestions.isNotEmpty()) {
            FlowRow(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                suggestions.forEach { suggestion ->
                    SuggestionChip(
                        onClick = { viewModel.sendMessage(suggestion) },
                        label = { Text(suggestion, fontSize = 11.sp) },
                        colors = SuggestionChipDefaults.suggestionChipColors(containerColor = DarkSurfaceVariant)
                    )
                }
            }
        }

        // Bottom Input Area
        Surface(
            color = DarkSurface,
            shadowElevation = 8.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { viewModel.inputText.value = it },
                    placeholder = { Text("Message ${activePersona.name}...", fontSize = 14.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryCyan,
                        unfocusedBorderColor = DarkSurfaceVariant
                    )
                )

                IconButton(
                    onClick = {
                        viewModel.sendMessage()
                        coroutineScope.launch {
                            if (messages.isNotEmpty()) listState.animateScrollToItem(messages.size - 1)
                        }
                    },
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = PrimaryCyan)
                }
            }
        }
    }
}
