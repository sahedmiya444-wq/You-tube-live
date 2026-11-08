package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkStudioColorScheme = darkColorScheme(
    primary = LiveNeonGreen,
    onPrimary = Color(0xFF003816),
    primaryContainer = Color(0xFF004D22),
    onPrimaryContainer = Color(0xFF80FFB7),
    secondary = InfoBlue,
    onSecondary = Color(0xFF00354D),
    secondaryContainer = Color(0xFF004D6E),
    onSecondaryContainer = Color(0xFFC2E8FF),
    tertiary = YouTubeRed,
    onTertiary = Color.White,
    tertiaryContainer = Color(0xFF6B0005),
    onTertiaryContainer = Color(0xFFFFDAD6),
    background = DarkBackground,
    onBackground = TextPrimary,
    surface = DarkSurface,
    onSurface = TextPrimary,
    surfaceVariant = DarkSurfaceVariant,
    onSurfaceVariant = TextSecondary,
    outline = DarkCardBorder,
    outlineVariant = Color(0xFF1E2230),
    error = YouTubeRed,
    onError = Color.White
)

@Composable
fun AutoLiveStudioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkStudioColorScheme,
        typography = Typography,
        content = content
    )
}
