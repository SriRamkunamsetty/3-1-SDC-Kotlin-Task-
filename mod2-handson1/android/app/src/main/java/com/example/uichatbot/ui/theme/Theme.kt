package com.example.uichatbot.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0F172A) // Deep Slate
val DarkSurface = Color(0xFF1E293B)    // Dark Surface
val DarkSurfaceVariant = Color(0xFF334155)

val PrimaryCyan = Color(0xFF38BDF8)     // User Bubble & Primary Accent
val AccentPurple = Color(0xFFA855F7)    // Code Mentor
val AccentEmerald = Color(0xFF34D399)   // Summarizer
val AccentAmber = Color(0xFFFBBF24)     // Creative Writer

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryCyan,
    onPrimary = Color.Black,
    secondary = AccentPurple,
    tertiary = AccentEmerald,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun UIChatbotTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
