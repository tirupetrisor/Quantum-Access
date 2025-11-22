package com.example.quantumaccess.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.PieChartOutline
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.ui.components.HeaderLogoutButton
import com.example.quantumaccess.ui.components.QuantumButton
import com.example.quantumaccess.ui.components.QuantumLogoBlueOrbits
import com.example.quantumaccess.ui.theme.DeepBlue
import com.example.quantumaccess.ui.theme.QuantumAccessTheme
import java.util.Locale

@Composable
fun AnalyticsDashboardScreen(
	modifier: Modifier = Modifier,
	onReturnToDashboard: () -> Unit = {},
	onLogout: () -> Unit = {}
) {
	val slices = remember {
		listOf(
			AnalyticsSlice("Quantum", 65f, DeepBlue),
			AnalyticsSlice("Normal", 30f, Color(0xFF9CA3AF)),
			AnalyticsSlice("Intercepted", 5f, Color(0xFFF97316))
		)
	}
	var showLogoutDialog by remember { mutableStateOf(false) }

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(Color(0xFFF5F6FB))
	) {
		Column(modifier = Modifier.fillMaxSize()) {
			AnalyticsHeader(onLogout = { showLogoutDialog = true })
			Box(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
					.padding(horizontal = 20.dp),
				contentAlignment = Alignment.Center
			) {
				AnalyticsCard(
					slices = slices,
					modifier = Modifier
						.fillMaxWidth()
						.widthIn(max = 380.dp)
				)
			}
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 20.dp, vertical = 24.dp)
			) {
				QuantumButton(
					text = "Return to Dashboard",
					onClick = onReturnToDashboard
				)
			}
		}

		if (showLogoutDialog) {
			AnalyticsLogoutDialog(
				onCancel = { showLogoutDialog = false },
				onConfirm = {
					showLogoutDialog = false
					onLogout()
				}
			)
		}
	}
}

@Composable
private fun AnalyticsHeader(onLogout: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.statusBarsPadding()
			.background(DeepBlue)
			.padding(horizontal = 16.dp, vertical = 14.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				Box(
					modifier = Modifier
						.size(36.dp)
						.clip(RoundedCornerShape(10.dp))
						.background(Color.White),
					contentAlignment = Alignment.Center
				) {
					QuantumLogoBlueOrbits(showText = false, iconSize = 22.dp)
				}
				Spacer(modifier = Modifier.width(10.dp))
				Column {
					Text(
						text = "QuantumAccess",
						color = Color.White,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold
					)
					Text(
						text = "Quantum Security Analytics",
						color = Color(0xFFCBD5E1),
						style = MaterialTheme.typography.labelSmall
					)
				}
			}
			HeaderLogoutButton(onClick = onLogout)
		}
	}
}

@Composable
private fun AnalyticsCard(
	slices: List<AnalyticsSlice>,
	modifier: Modifier = Modifier.fillMaxWidth()
) {
	Surface(
		shape = RoundedCornerShape(24.dp),
		color = Color.White,
		shadowElevation = 6.dp,
		modifier = modifier
			.heightIn(min = 260.dp)
	) {
		Column(
			modifier = Modifier.padding(horizontal = 18.dp, vertical = 22.dp)
		) {
			Row(
				modifier = Modifier.fillMaxWidth(),
				horizontalArrangement = Arrangement.SpaceBetween,
				verticalAlignment = Alignment.CenterVertically
			) {
				Text(
					text = "Transaction Security Distribution",
					style = MaterialTheme.typography.titleMedium,
					color = Color(0xFF0F172A),
					fontWeight = FontWeight.SemiBold
				)
				Box(
					modifier = Modifier
						.size(32.dp)
						.clip(CircleShape)
						.background(Color(0xFFF1F5F9)),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Outlined.PieChartOutline,
						contentDescription = null,
						tint = DeepBlue
					)
				}
			}
			Spacer(modifier = Modifier.height(18.dp))
			AnalyticsChart(slices = slices)
		}
	}
}

@Composable
private fun AnalyticsChart(slices: List<AnalyticsSlice>) {
	val total = slices.sumOf { it.value.toDouble() }.toFloat().coerceAtLeast(1f)
	val chartSize = 200.dp
	val sliceAngles = remember(slices) {
		var start = -90f
		slices.map { slice ->
			val sweep = (slice.value / total) * 360f
			val info = SliceAngle(slice, start, sweep)
			start += sweep
			info
		}
	}

	Column(
		horizontalAlignment = Alignment.CenterHorizontally,
		verticalArrangement = Arrangement.spacedBy(18.dp),
		modifier = Modifier
			.fillMaxWidth()
			.heightIn(min = 200.dp)
	) {
		Box(contentAlignment = Alignment.Center) {
			Canvas(modifier = Modifier.size(chartSize)) {
				val strokeWidth = size.minDimension * 0.18f
				val arcDiameter = size.minDimension - strokeWidth
				val center = Offset(size.width / 2, size.height / 2)

				sliceAngles.forEach { info ->
					drawArc(
						color = info.slice.color,
						startAngle = info.startAngle,
						sweepAngle = info.sweepAngle,
						useCenter = false,
						topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
						size = Size(arcDiameter, arcDiameter),
						style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
					)
				}

				drawCircle(
					color = Color.White,
					radius = (arcDiameter / 2f) - strokeWidth / 4f,
					center = center
				)
			}
		}

		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceEvenly
		) {
			slices.forEach { slice ->
				LegendEntry(slice = slice)
			}
		}
	}
}

@Composable
private fun LegendEntry(slice: AnalyticsSlice) {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Box(
				modifier = Modifier
					.size(10.dp)
					.clip(RoundedCornerShape(50))
					.background(slice.color)
			)
			Spacer(modifier = Modifier.width(6.dp))
			Text(
				text = slice.label,
				color = Color(0xFF1F2937),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.SemiBold
			)
		}
		Spacer(modifier = Modifier.height(4.dp))
		Text(
			text = formatPercentage(slice.value),
			color = slice.color,
			style = MaterialTheme.typography.bodyMedium,
			fontWeight = FontWeight.Bold
		)
	}
}

@Composable
private fun AnalyticsLogoutDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.45f)),
		contentAlignment = Alignment.Center
	) {
		Surface(
			shape = RoundedCornerShape(20.dp),
			color = Color.White,
			shadowElevation = 12.dp,
			modifier = Modifier.padding(horizontal = 24.dp)
		) {
			Column(modifier = Modifier.padding(20.dp)) {
				Text(
					text = "Log Out",
					style = MaterialTheme.typography.titleMedium,
					color = Color(0xFF111827),
					fontWeight = FontWeight.SemiBold
				)
				Spacer(modifier = Modifier.height(8.dp))
				Text(
					text = "Are you sure you want to log out?",
					style = MaterialTheme.typography.bodySmall,
					color = Color(0xFF6B7280)
				)
				Spacer(modifier = Modifier.height(18.dp))
				Row(
					modifier = Modifier.fillMaxWidth(),
					horizontalArrangement = Arrangement.spacedBy(12.dp)
				) {
					Surface(
						shape = RoundedCornerShape(14.dp),
						color = Color(0xFFF3F4F6),
						modifier = Modifier
							.weight(1f)
					) {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 12.dp)
								.clickable { onCancel() }
						) {
							Text(
								text = "Cancel",
								color = Color(0xFF374151),
								style = MaterialTheme.typography.labelLarge,
								fontWeight = FontWeight.Medium
							)
						}
					}
					Surface(
						shape = RoundedCornerShape(14.dp),
						color = DeepBlue,
						modifier = Modifier
							.weight(1f)
					) {
						Box(
							contentAlignment = Alignment.Center,
							modifier = Modifier
								.fillMaxWidth()
								.padding(vertical = 12.dp)
								.clickable { onConfirm() }
						) {
							Text(
								text = "Confirm",
								color = Color.White,
								style = MaterialTheme.typography.labelLarge,
								fontWeight = FontWeight.Medium
							)
						}
					}
				}
			}
		}
	}
}

private data class AnalyticsSlice(
	val label: String,
	val value: Float,
	val color: Color
)

private data class SliceAngle(
	val slice: AnalyticsSlice,
	val startAngle: Float,
	val sweepAngle: Float
)

private fun formatPercentage(value: Float): String {
	return String.format(Locale.US, "%.1f%%", value).replace('.', ',')
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AnalyticsDashboardPreview() {
	QuantumAccessTheme {
		AnalyticsDashboardScreen()
	}
}

