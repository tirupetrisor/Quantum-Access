package com.example.quantumaccess.ui.screens

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.ui.components.QuantumLogo
import com.example.quantumaccess.ui.theme.DeepBlue
import com.example.quantumaccess.ui.theme.SecureGreen

@Composable
fun LocationVerificationScreen(
	modifier: Modifier = Modifier,
	onUseCurrentLocation: () -> Unit = {},
	onEnterLocationManually: () -> Unit = {}
) {
	Box(
		modifier = modifier
			.fillMaxSize()
			.padding(horizontal = 24.dp, vertical = 16.dp)
			.widthIn(max = 480.dp),
		contentAlignment = Alignment.Center
	) {
		// Header-like logo at the top-left
		QuantumLogo(
			iconSize = 32.dp,
			showText = true,
			modifier = Modifier
				.align(Alignment.TopStart)
				.padding(top = 35.dp)
		)

		// Place the content slightly below exact center
		Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(top = 24.dp)) {
			Spacer(modifier = Modifier.height(25.dp))

			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.Center,
				verticalAlignment = Alignment.CenterVertically
			) {
				StatusBadgeAuthorized(modifier = Modifier.align(Alignment.CenterVertically))
			}

			Spacer(modifier = Modifier.height(16.dp))

			Surface(
				shape = RoundedCornerShape(24.dp),
				shadowElevation = 10.dp,
				color = Color.White,
				modifier = Modifier
					.fillMaxWidth()
					.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(24.dp))
			) {
				Column(modifier = Modifier.padding(16.dp)) {
					MapVisual(modifier = Modifier
						.fillMaxWidth()
						.height(300.dp)
						.clip(RoundedCornerShape(16.dp))
						.background(Color(0xFFF8FAFC)))

					Spacer(modifier = Modifier.height(16.dp))

					Row(
						modifier = Modifier.fillMaxWidth(),
						horizontalArrangement = Arrangement.Center,
						verticalAlignment = Alignment.CenterVertically
					) {
						Icon(
							imageVector = Icons.Filled.LocationOn,
							contentDescription = null,
							tint = DeepBlue,
							modifier = Modifier.size(18.dp)
						)
						Spacer(modifier = Modifier.size(8.dp))
						Text(
							text = "Location validated via GPS",
							color = DeepBlue,
							style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
						)
					}
					Spacer(modifier = Modifier.height(6.dp))
					Text(
						text = "Matched to nearest quantum node: Node-07 • 42 m",
						color = Color(0xFF6B7280),
						style = MaterialTheme.typography.bodySmall,
						modifier = Modifier.fillMaxWidth(),
						textAlign = TextAlign.Center
					)

					Spacer(modifier = Modifier.height(16.dp))

					Button(
						onClick = onUseCurrentLocation,
						colors = ButtonDefaults.buttonColors(containerColor = DeepBlue, contentColor = Color.White),
						shape = RoundedCornerShape(16.dp),
						modifier = Modifier.fillMaxWidth()
					) {
						Icon(imageVector = Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
						Spacer(modifier = Modifier.size(8.dp))
						Text(text = "Use Current Location")
					}
					Spacer(modifier = Modifier.height(10.dp))
					OutlinedButton(
						onClick = onEnterLocationManually,
						shape = RoundedCornerShape(16.dp),
						border = BorderStroke(1.dp, DeepBlue),
						colors = ButtonDefaults.outlinedButtonColors(contentColor = DeepBlue),
						modifier = Modifier.fillMaxWidth()
					) {
						Icon(imageVector = Icons.Filled.Keyboard, contentDescription = null, modifier = Modifier.size(18.dp), tint = DeepBlue)
						Spacer(modifier = Modifier.size(8.dp))
						Text(text = "Enter Location Manually", color = DeepBlue)
					}
				}
			}
		}
	}
}

@Composable
private fun StatusBadgeAuthorized(modifier: Modifier = Modifier) {
	Surface(
		shape = RoundedCornerShape(22.dp),
		shadowElevation = 6.dp,
		color = Color.White,
		modifier = modifier
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.border(1.dp, Color(0xFFE5E7EB), RoundedCornerShape(22.dp))
				.padding(horizontal = 14.dp, vertical = 8.dp)
		) {
			Box(
				modifier = Modifier
					.size(10.dp)
					.background(SecureGreen, CircleShape)
			)
			Spacer(modifier = Modifier.size(8.dp))
			Icon(
				imageVector = Icons.Filled.CheckCircle,
				contentDescription = null,
				tint = SecureGreen,
				modifier = Modifier.size(16.dp)
			)
			Spacer(modifier = Modifier.size(8.dp))
			Text(text = "Authorized Access", color = DeepBlue, fontSize = 14.sp)
		}
	}
}

@Composable
private fun MapVisual(modifier: Modifier = Modifier) {
	Box(modifier = modifier, contentAlignment = Alignment.Center) {
		// faint constellation dots
		Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
			val dots = listOf(
				Offset(40f, 24f), Offset(120f, 48f), Offset(220f, 20f),
				Offset(80f, 100f), Offset(180f, 90f), Offset(240f, 120f),
				Offset(60f, 170f), Offset(150f, 180f), Offset(220f, 190f)
			)
			dots.forEach { p ->
				drawCircle(color = Color(0xFFD1D5DB), radius = 3f, center = p)
			}
		}

		// center marker with pulse
		PulsingMarker()
	}
}

@Composable
private fun PulsingMarker() {
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
		// glow ring
		Canvas(modifier = Modifier.size(64.dp).scale(scale)) {
			drawCircle(
				color = SecureGreen.copy(alpha = 0.25f),
				radius = size.minDimension / 2f,
				style = Stroke(width = size.minDimension * 0.08f, cap = StrokeCap.Round)
			)
		}
		// pin
		Icon(
			imageVector = Icons.Filled.LocationOn,
			contentDescription = null,
			tint = SecureGreen,
			modifier = Modifier.size(28.dp)
		)
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun LocationVerificationPreview() {
	LocationVerificationScreen()
}



