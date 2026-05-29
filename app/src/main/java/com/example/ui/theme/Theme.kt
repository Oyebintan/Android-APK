package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = ElectricPurple,
    secondary = NeonCyan,
    tertiary = PurplePrimary,
    background = DarkBg,
    surface = DarkSurface,
    surfaceVariant = DarkSurfaceVariant,
    onBackground = TextPrimary,
    onSurface = TextPrimary,
    onPrimary = Color.White,
    onSecondary = Color.Black
)

private val LightColorScheme = lightColorScheme(
    primary = ElectricPurple,
    secondary = NeonCyan,
    tertiary = PurplePrimary,
    background = Color(0xFFF9FAFB),
    surface = Color.White,
    surfaceVariant = Color(0xFFF3F4F6),
    onBackground = Color(0xFF111827),
    onSurface = Color(0xFF111827),
    onPrimary = Color.White,
    onSecondary = Color.Black
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
