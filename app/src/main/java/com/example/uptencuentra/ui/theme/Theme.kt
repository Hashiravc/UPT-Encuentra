package com.example.uptencuentra.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = UptPrimary,
    onPrimary = UptOnPrimary,
    secondary = UptSecondary,
    background = UptBackground,
    surface = UptSurface
)

@Composable
fun UPTEncuentraTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = LightColors,
        typography = Typography,
        content = content
    )
}