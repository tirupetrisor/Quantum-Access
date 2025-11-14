package com.example.quantumaccess.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.ui.theme.DeepBlue
import com.example.quantumaccess.ui.theme.SecureGreen
import com.example.quantumaccess.ui.theme.Silver
import kotlinx.coroutines.delay

private val BackgroundBlue = DeepBlue
private val NeonGreen = SecureGreen

@Composable
fun SplashScreen(modifier: Modifier = Modifier, onContinue: () -> Unit = {}) {
    var showTagline by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(900)
        showTagline = true
        delay(900)
        onContinue()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            GlassIconCard()
            Spacer(modifier = Modifier.height(24.dp))
            BrandTitle()
            Spacer(modifier = Modifier.height(12.dp))
            AnimatedVisibility(visible = showTagline, enter = fadeIn(), exit = fadeOut()) {
                Text(
                    text = "Quantum-secured banking access",
                    color = Silver.copy(alpha = 0.85f),
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun GlassIconCard(size: Dp = 128.dp) {
    Box(
        modifier = Modifier
            .size(size)
            .background(
                color = Color(0x0DFFFFFF), // ~5% white for glass-like surface
                shape = RoundedCornerShape(24.dp)
            )
            .border(width = 1.dp, color = Color.White.copy(alpha = 0.12f), shape = RoundedCornerShape(24.dp))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        AtomIcon(modifier = Modifier.size(80.dp).align(Alignment.Center).offset(x = (-4).dp))
    }
}

@Composable
private fun AtomIcon(modifier: Modifier = Modifier) {
    val gradient = Brush.linearGradient(listOf(Silver, NeonGreen))
    Canvas(modifier = modifier) {
        val s = size.minDimension / 80f
        val center = Offset(size.width / 2f, size.height / 2f)

        withTransform({
            translate(center.x, center.y)
            scale(0.86f, 0.86f)
            translate(-center.x, -center.y)
        }) {
            // nucleus (r = 4)
            drawCircle(brush = gradient, radius = 4f * s, center = center)

            // orbits
            val stroke = Stroke(width = 2f * s, cap = StrokeCap.Round)
            val rx = 30f * s
            val ry = 12f * s
            val ovalSize = Size(width = rx * 2f, height = ry * 2f)
            val topLeft = Offset(center.x - rx, center.y - ry)

            drawOval(brush = gradient, topLeft = topLeft, size = ovalSize, style = stroke)
            rotateAroundCenter(60f) {
                drawOval(brush = gradient, topLeft = topLeft, size = ovalSize, style = stroke)
            }
            rotateAroundCenter(120f) {
                drawOval(brush = gradient, topLeft = topLeft, size = ovalSize, style = stroke)
            }

            // electrons: (65,40) green, (25,50) silver, (55,25) green; r=3
            val eR = 3f * s
            drawCircle(color = NeonGreen, radius = eR, center = Offset(center.x + (65f - 40f) * s, center.y + (40f - 40f) * s))
            drawCircle(color = Silver, radius = eR, center = Offset(center.x + (25f - 40f) * s, center.y + (50f - 40f) * s))
            drawCircle(color = NeonGreen, radius = eR, center = Offset(center.x + (55f - 40f) * s, center.y + (25f - 40f) * s))
        }
    }
}

private inline fun androidx.compose.ui.graphics.drawscope.DrawScope.rotateAroundCenter(
    degrees: Float,
    block: androidx.compose.ui.graphics.drawscope.DrawScope.() -> Unit
) {
    rotate(degrees = degrees, pivot = Offset(size.width / 2f, size.height / 2f)) {
        block()
    }
}

@Composable
private fun BrandTitle() {
    val gradient = Brush.linearGradient(listOf(Silver, NeonGreen))
    val text = buildAnnotatedString {
        withStyle(SpanStyle(brush = gradient, fontWeight = FontWeight.Bold)) {
            append("Quantum")
        }
        withStyle(SpanStyle(color = Color.White, fontWeight = FontWeight.Bold)) {
            append("Access")
        }
    }
    Text(
        text = text,
        fontSize = 36.sp,
        letterSpacing = 0.5.sp
    )
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun SplashPreview() {
    SplashScreen()
}


