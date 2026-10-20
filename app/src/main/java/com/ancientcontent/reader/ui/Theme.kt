package com.ancientcontent.reader.ui

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

private val LightColors = lightColorScheme(
    primary = Color(0xFF8A5F14),
    onPrimary = Color.White,
    background = Color(0xFFFAF7F2),
    onBackground = Color(0xFF1F1A14),
    surface = Color(0xFFFFFFFF),
    onSurface = Color(0xFF1F1A14),
    surfaceVariant = Color(0xFFEFE8DB),
    onSurfaceVariant = Color(0xFF5B5140),
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFFD4A24A),
    onPrimary = Color(0xFF1F1A14),
    background = Color(0xFF14110D),
    onBackground = Color(0xFFEFE8DB),
    surface = Color(0xFF1C1813),
    onSurface = Color(0xFFEFE8DB),
    surfaceVariant = Color(0xFF2A241B),
    onSurfaceVariant = Color(0xFFC9BFA9),
)

private val base = Typography()

private val AppTypography = base.copy(
    titleLarge = base.titleLarge.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.Bold),
    titleMedium = base.titleMedium.copy(fontFamily = FontFamily.Serif, fontWeight = FontWeight.SemiBold),
)

@Composable
fun AncientContentTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) DarkColors else LightColors,
        typography = AppTypography,
        content = content,
    )
}
