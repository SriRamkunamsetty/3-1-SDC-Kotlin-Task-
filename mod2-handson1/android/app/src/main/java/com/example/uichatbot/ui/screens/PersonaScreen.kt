package com.example.uichatbot.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uichatbot.data.model.BotPersona
import com.example.uichatbot.ui.components.BotAvatarIcon
import com.example.uichatbot.ui.theme.*
import com.example.uichatbot.viewmodel.ChatViewModel
import com.example.uichatbot.viewmodel.UiState

@Composable
fun PersonaScreen(viewModel: ChatViewModel, onPersonaSelected: () -> Unit) {
    val state by viewModel.personasState.collectAsState()
    val activePersona by viewModel.activePersona.collectAsState()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Select AI Bot Persona",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = PrimaryCyan
            )
            Text(
                text = "Choose an AI personality to customize system prompt behavior and conversational tone.",
                fontSize = 14.sp,
                color = TextSecondary
            )
        }

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

                items(data.personas) { persona ->
                    PersonaCard(
                        persona = persona,
                        isSelected = (persona.id == activePersona.id),
                        onSelect = {
                            viewModel.selectPersona(persona)
                            onPersonaSelected()
                        }
                    )
                }
            }
            is UiState.Idle -> {}
        }
    }
}

@Composable
fun PersonaCard(persona: BotPersona, isSelected: Boolean, onSelect: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) DarkSurfaceVariant else DarkSurface
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            BotAvatarIcon(persona.id)

            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text(persona.name, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
                    if (isSelected) {
                        Surface(
                            color = PrimaryCyan,
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text("ACTIVE", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.Black, modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp))
                        }
                    }
                }
                Text(persona.title, fontSize = 12.sp, color = PrimaryCyan, fontWeight = FontWeight.Medium)
                Text(persona.greeting, fontSize = 12.sp, color = TextSecondary)
            }

            Button(
                onClick = onSelect,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isSelected) PrimaryCyan else DarkSurfaceVariant
                )
            ) {
                Text(
                    text = if (isSelected) "Active" else "Select",
                    fontSize = 11.sp,
                    color = if (isSelected) Color.Black else TextPrimary
                )
            }
        }
    }
}
