package com.example.quantumaccess.core.designsystem.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import com.example.quantumaccess.core.designsystem.theme.Silver

@Composable
fun QuantumLogoNeonCard(
    modifier: Modifier = Modifier,
    size: Dp = 128.dp,
    cornerRadius: Dp = 24.dp,
    iconOffsetX: Dp = (-4).dp
) {
    val padding = size * 0.15625f
    val iconSize = size * 0.625f

    Box(
        modifier = modifier
            .size(size)
            .background(
                color = Color(0x0DFFFFFF),
                shape = RoundedCornerShape(cornerRadius)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.12f),
                shape = RoundedCornerShape(cornerRadius)
            )
            .padding(padding),
        contentAlignment = Alignment.Center
    ) {
        QuantumNeonAtom(
            modifier = Modifier
                .size(iconSize)
                .offset(x = iconOffsetX)
        )
    }
}

@Composable
private fun QuantumNeonAtom(modifier: Modifier = Modifier) {
    val gradient = Brush.linearGradient(listOf(Silver, SecureGreen))
    Canvas(modifier = modifier) {
        val s = size.minDimension / 80f
        val center = Offset(size.width / 2f, size.height / 2f)

        withTransform({
            translate(center.x, center.y)
            scale(0.86f, 0.86f)
            translate(-center.x, -center.y)
        }) {
            drawCircle(brush = gradient, radius = 4f * s, center = center)

            val stroke = Stroke(width = 2f * s, cap = StrokeCap.Round)
            val rx = 30f * s
            val ry = 12f * s
            val ovalSize = Size(width = rx * 2f, height = ry * 2f)
            val topLeft = Offset(center.x - rx, center.y - ry)

            drawOval(brush = gradient, topLeft = topLeft, size = ovalSize, style = stroke)
            rotateAround(center, 60f) {
                drawOval(brush = gradient, topLeft = topLeft, size = ovalSize, style = stroke)
            }
            rotateAround(center, 120f) {
                drawOval(brush = gradient, topLeft = topLeft, size = ovalSize, style = stroke)
            }

            val electronRadius = 3f * s
            drawCircle(color = SecureGreen, radius = electronRadius, center = Offset(center.x + (65f - 40f) * s, center.y + (40f - 40f) * s))
            drawCircle(color = Silver, radius = electronRadius, center = Offset(center.x + (25f - 40f) * s, center.y + (50f - 40f) * s))
            drawCircle(color = SecureGreen, radius = electronRadius, center = Offset(center.x + (55f - 40f) * s, center.y + (25f - 40f) * s))
        }
    }
}

private inline fun androidx.compose.ui.graphics.drawscope.DrawScope.rotateAround(
    center: Offset,
    degrees: Float,
    block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    rotate(degrees = degrees, pivot = center) {
        block()
    }
}

