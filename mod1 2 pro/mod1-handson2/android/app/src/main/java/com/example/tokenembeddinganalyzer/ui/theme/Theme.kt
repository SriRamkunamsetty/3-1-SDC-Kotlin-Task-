package com.example.tokenembeddinganalyzer.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkBackground = Color(0xFF0F172A) // Deep Slate
val DarkSurface = Color(0xFF1E293B)    // Dark Slate
val DarkSurfaceVariant = Color(0xFF334155)

val PrimaryBlue = Color(0xFF38BDF8)     // Electric Cyan
val SecondaryPurple = Color(0xFFA855F7) // Vibrant Violet
val AccentEmerald = Color(0xFF34D399)   // Neon Emerald
val AccentAmber = Color(0xFFFBBF24)     // Amber Accent

val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryBlue,
    onPrimary = Color.Black,
    secondary = SecondaryPurple,
    onSecondary = Color.White,
    tertiary = AccentEmerald,
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary
)

@Composable
fun TokenEmbeddingTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkColorScheme,
        content = content
    )
}
