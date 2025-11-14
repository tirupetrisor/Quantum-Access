package com.example.quantumaccess.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.ui.theme.DeepBlue

@Composable
fun QuantumLogo(
    modifier: Modifier = Modifier,
    iconSize: Dp = 40.dp,
    showText: Boolean = true
) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = modifier) {
        Box(
            modifier = Modifier
                .size(iconSize)
                .background(color = DeepBlue, shape = RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
        ) {
            AtomMark(modifier = Modifier.size(iconSize * 0.6f))
        }
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

@Composable
private fun AtomMark(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val s = size.minDimension / 80f
        val center = Offset(size.width / 2f, size.height / 2f)

        // nucleus
        drawCircle(color = Color.White, radius = 3f * s, center = center)

        // three orbits, thicker stroke, rounded caps
        val stroke = Stroke(width = 8f * s, cap = StrokeCap.Round)
        val rx = 28f * s
        val ry = 12f * s
        val ovalSize = Size(width = rx * 2f, height = ry * 2f)
        val topLeft = Offset(center.x - rx, center.y - ry)

        drawOval(color = Color.White, topLeft = topLeft, size = ovalSize, style = stroke)
        rotate(degrees = 60f, pivot = center) {
            drawOval(color = Color.White, topLeft = topLeft, size = ovalSize, style = stroke)
        }
        rotate(degrees = 120f, pivot = center) {
            drawOval(color = Color.White, topLeft = topLeft, size = ovalSize, style = stroke)
        }
    }
}

