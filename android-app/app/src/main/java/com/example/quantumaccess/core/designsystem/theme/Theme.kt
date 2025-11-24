package com.example.quantumaccess.core.designsystem.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = SecureGreen,
    secondary = Silver,
    tertiary = DeepBlue
)

private val LightColorScheme = lightColorScheme(
    primary = SecureGreen,
    secondary = Silver,
    tertiary = DeepBlue
)

@Composable
fun QuantumAccessTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    MaterialTheme(
        colorScheme = colorScheme,
        content = content
    )
}


