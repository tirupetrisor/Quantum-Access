package com.example.quantumaccess.feature.location.presentation.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.Icon
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.core.designsystem.theme.SecureGreen

@Composable
fun MapVisual(authorized: Boolean?, modifier: Modifier = Modifier) {
	Box(modifier = modifier, contentAlignment = Alignment.Center) {
		Canvas(modifier = Modifier.fillMaxSize()) {
			val dots = listOf(
				Offset(40f, 24f), Offset(120f, 48f), Offset(220f, 20f),
				Offset(80f, 100f), Offset(180f, 90f), Offset(240f, 120f),
				Offset(60f, 170f), Offset(150f, 180f), Offset(220f, 190f)
			)
			dots.forEach { p ->
				drawCircle(color = Color(0xFFD1D5DB), radius = 3f, center = p)
			}
		}
		PulsingMarker(
			color = when (authorized) {
				true -> SecureGreen
				false -> Color(0xFFD32F2F)
				else -> Color(0xFF9E9E9E)
			}
		)
	}
}

@Composable
fun PulsingMarker(color: Color) {
	val infinite = rememberInfiniteTransition(label = "marker")
	val scale by infinite.animateFloat(
		initialValue = 0.9f,
		targetValue = 1.15f,
		animationSpec = infiniteRepeatable(
			animation = tween(1400, easing = LinearEasing),
			repeatMode = RepeatMode.Reverse
		),
		label = "scale"
	)
	Box(contentAlignment = Alignment.Center) {
		Canvas(modifier = Modifier.size(64.dp).scale(scale)) {
			drawCircle(
				color = color.copy(alpha = 0.25f),
				radius = size.minDimension / 2f,
				style = Stroke(width = size.minDimension * 0.08f, cap = StrokeCap.Round)
			)
		}
		Icon(
			imageVector = Icons.Filled.LocationOn,
			contentDescription = null,
			tint = color
		)
	}
}


