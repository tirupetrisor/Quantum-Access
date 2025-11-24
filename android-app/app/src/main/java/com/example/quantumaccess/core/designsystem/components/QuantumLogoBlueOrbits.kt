package com.example.quantumaccess.core.designsystem.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.theme.DeepBlue

@Composable
fun QuantumLogoBlueOrbits(
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    showText: Boolean = true,
    iconScale: Float = 0.9f
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        QuantumLogoGradientBadge(
            size = iconSize,
            orbitColor = DeepBlue,
            gradientColors = listOf(Color.White, Color.White),
            iconScale = iconScale,
            cornerRadius = 12.dp
        )
        if (showText) {
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "QuantumAccess",
                fontSize = 22.sp,
                color = DeepBlue,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
