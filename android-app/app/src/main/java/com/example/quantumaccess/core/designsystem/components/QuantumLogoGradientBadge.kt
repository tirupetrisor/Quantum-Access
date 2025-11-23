package com.example.quantumaccess.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun QuantumLogoGradientBadge(
    modifier: Modifier = Modifier,
    size: Dp = 44.dp,
    orbitColor: Color = Color.White,
    gradientColors: List<Color> = listOf(Color(0xFF1A237E), Color(0xFF3F51B5)),
    cornerRadius: Dp = 12.dp,
    iconScale: Float = 0.5f
) {
    val iconSize = size * iconScale.coerceIn(0.3f, 1f)
    val colors = when {
        gradientColors.isEmpty() -> listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
        gradientColors.size == 1 -> List(2) { gradientColors.first() }
        else -> gradientColors
    }

    Box(
        modifier = modifier
            .size(size)
            .background(
                brush = Brush.linearGradient(colors),
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        QuantumOrbitGlyph(
            modifier = Modifier.size(iconSize),
            orbitColor = orbitColor
        )
    }
}

@Composable
private fun QuantumOrbitGlyph(
    modifier: Modifier = Modifier,
    orbitColor: Color
) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 80f
        val center = Offset(size.width / 2f, size.height / 2f)

        drawCircle(color = orbitColor, radius = 4f * s, center = center)

        val stroke = Stroke(width = 8f * s, cap = StrokeCap.Round)
        val rx = 28f * s
        val ry = 12f * s
        val ovalSize = Size(width = rx * 2f, height = ry * 2f)
        val topLeft = Offset(center.x - rx, center.y - ry)

        drawOval(color = orbitColor, topLeft = topLeft, size = ovalSize, style = stroke)
        rotate(degrees = 60f, pivot = center) {
            drawOval(color = orbitColor, topLeft = topLeft, size = ovalSize, style = stroke)
        }
        rotate(degrees = 120f, pivot = center) {
            drawOval(color = orbitColor, topLeft = topLeft, size = ovalSize, style = stroke)
        }
    }
}

