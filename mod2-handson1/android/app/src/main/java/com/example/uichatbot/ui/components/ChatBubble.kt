package com.example.uichatbot.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.uichatbot.data.model.ChatMessage
import com.example.uichatbot.data.model.MessageSender
import com.example.uichatbot.ui.theme.*

@Composable
fun ChatBubble(message: ChatMessage) {
    val isUser = message.sender == MessageSender.USER

    val bubbleShape = if (isUser) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 4.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomEnd = 16.dp, bottomStart = 4.dp)
    }

    val bubbleColor = if (isUser) PrimaryCyan else DarkSurface

    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        if (!isUser) {
            BotAvatarIcon(message.personaId)
            Spacer(modifier = Modifier.width(8.dp))
        }

        Column(
            horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
        ) {
            Surface(
                color = bubbleColor,
                shape = bubbleShape,
                shadowElevation = 2.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    if (message.text.contains("```")) {
                        FormattedCodeMessage(message.text)
                    } else {
                        Text(
                            text = message.text,
                            color = if (isUser) Color.Black else TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = if (isUser) FontWeight.Medium else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = message.timestamp,
                fontSize = 10.sp,
                color = TextSecondary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
        }

        if (isUser) {
            Spacer(modifier = Modifier.width(8.dp))
            UserAvatarIcon()
        }
    }
}

@Composable
fun BotAvatarIcon(personaId: String) {
    val (icon, bgCol) = when (personaId) {
        "CODE_MENTOR" -> Icons.Default.Code to AccentPurple
        "CONCISE_SUMMARIZER" -> Icons.Default.Compress to AccentEmerald
        "CREATIVE_WRITER" -> Icons.Default.AutoAwesome to AccentAmber
        else -> Icons.Default.SmartToy to PrimaryCyan
    }

    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(bgCol.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, contentDescription = "Bot Avatar", tint = bgCol, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun UserAvatarIcon() {
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(CircleShape)
            .background(PrimaryCyan.copy(alpha = 0.2f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(Icons.Default.Person, contentDescription = "User Avatar", tint = PrimaryCyan, modifier = Modifier.size(18.dp))
    }
}

@Composable
fun FormattedCodeMessage(text: String) {
    val parts = text.split("```")
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        parts.forEachIndexed { index, part ->
            if (index % 2 == 1) {
                // Code block snippet
                val codeLines = part.lines()
                val lang = codeLines.firstOrNull()?.trim() ?: "code"
                val codeBody = if (codeLines.size > 1) codeLines.drop(1).joinToString("\n") else part

                Surface(
                    color = DarkSurfaceVariant,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(lang.uppercase(), fontSize = 10.sp, fontWeight = FontWeight.Bold, color = PrimaryCyan)
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = codeBody.trim(),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            color = TextPrimary
                        )
                    }
                }
            } else {
                if (part.trim().isNotEmpty()) {
                    Text(text = part.trim(), color = TextPrimary, fontSize = 14.sp)
                }
            }
        }
    }
}
