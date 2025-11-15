package com.example.quantumaccess.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.ui.components.QuantumLogo
import com.example.quantumaccess.ui.theme.DeepBlue

@Composable
fun BiometricLoginScreen(
	modifier: Modifier = Modifier,
	onAuthenticate: () -> Unit = {}
) {
	Box(
		modifier = modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp, vertical = 24.dp),
		contentAlignment = Alignment.Center
	) {
		// Branding placed slightly lower from the very top
		QuantumLogo(
			iconSize = 40.dp,
			showText = true,
			modifier = Modifier
				.align(Alignment.TopCenter)
				.padding(top = 40.dp)
		)

		// Centered group: title + fingerprint
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Text(
				text = "Authenticate to access\nQuantum Bank",
				style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.SemiBold),
				color = DeepBlue,
				textAlign = TextAlign.Center,
				lineHeight = 32.sp
			)
			Spacer(modifier = Modifier.height(32.dp))
			FingerprintPulseButton(onClick = onAuthenticate)
		}
	}
}

@Composable
private fun FingerprintPulseButton(
	modifier: Modifier = Modifier,
	onClick: () -> Unit = {}
) {
	val transition = rememberInfiniteTransition(label = "pulse")
	val scale by transition.animateFloat(
		initialValue = 1f,
		targetValue = 1.04f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 1600, easing = LinearEasing),
			repeatMode = RepeatMode.Reverse
		),
		label = "scale"
	)
	val glowAlpha by transition.animateFloat(
		initialValue = 0.28f,
		targetValue = 0.08f,
		animationSpec = infiniteRepeatable(
			animation = tween(durationMillis = 1600, easing = LinearEasing),
			repeatMode = RepeatMode.Reverse
		),
		label = "alpha"
	)

	// Outer soft glow ring
	Box(
		modifier = modifier
			.size(192.dp)
			.graphicsLayer { shadowElevation = 0f }
			.background(
				color = Color.Transparent,
				shape = CircleShape
			),
		contentAlignment = Alignment.Center
	) {
		Box(
			modifier = Modifier
				.size(176.dp)
				.scale(scale)
				.background(
					brush = Brush.linearGradient(
						colors = listOf(DeepBlue.copy(alpha = glowAlpha), Color(0xFF0D47A1).copy(alpha = 0f))
					),
					shape = CircleShape
				)
		)

		// Main fingerprint button
		Box(
			modifier = Modifier
				.size(160.dp)
				.scale(scale)
				.background(
					brush = Brush.linearGradient(colors = listOf(DeepBlue, Color(0xFF0D47A1))),
					shape = CircleShape
				)
				.clickable(onClick = onClick),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Filled.Fingerprint,
				contentDescription = "Fingerprint",
				tint = Color.White,
				modifier = Modifier.size(72.dp)
			)
		}
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun BiometricLoginPreview() {
	BiometricLoginScreen(onAuthenticate = {})
}

