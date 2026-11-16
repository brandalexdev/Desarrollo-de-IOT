package com.iot.controlluces.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkIoTColorScheme = darkColorScheme(
    primary = AmberLightOn,
    onPrimary = SlateDark,
    secondary = CyanAuto,
    onSecondary = SlateDark,
    tertiary = EmeraldActive,
    background = SlateDark,
    onBackground = Color(0xFFF8FAFC),
    surface = SlateCard,
    onSurface = Color(0xFFF1F5F9),
    surfaceVariant = SlateSurface,
    onSurfaceVariant = Color(0xFFCBD5E1)
)

@Composable
fun ControlLucesEdificioTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = DarkIoTColorScheme,
        content = content
    )
}
