package com.example.quantumaccess.feature.analytics.presentation

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.runtime.remember
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
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.designsystem.components.QuantumTopBar
import com.example.quantumaccess.core.designsystem.theme.AccentOrange
import com.example.quantumaccess.core.designsystem.theme.CardBone
import com.example.quantumaccess.core.designsystem.theme.Cloud100
import com.example.quantumaccess.core.designsystem.theme.Cloud200
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Gunmetal
import com.example.quantumaccess.core.designsystem.theme.MistBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.QuantumAccessTheme
import com.example.quantumaccess.core.designsystem.theme.Steel200
import com.example.quantumaccess.data.sample.RepositoryProvider
import com.example.quantumaccess.domain.model.AnalyticsCategory
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.repository.TransactionRepository
import java.util.Locale

@Composable
fun AnalyticsDashboardScreen(
	modifier: Modifier = Modifier,
	onReturnToDashboard: () -> Unit = {},
	onLogout: () -> Unit = {},
	transactionRepository: TransactionRepository = RepositoryProvider.transactionRepository
) {
	val slices = remember(transactionRepository) { transactionRepository.getSecurityDistribution() }
	val uiSlices = remember(slices) { slices.map { it.toUiSlice() } }

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(Cloud100)
	) {
		Column(modifier = Modifier.fillMaxSize()) {
			QuantumTopBar(
				title = "QuantumAccess",
				subtitle = "Quantum Security Analytics",
				showLogoutButton = true,
				onLogoutClick = onLogout
			)
			Box(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
					.padding(horizontal = 20.dp),
				contentAlignment = Alignment.Center
			) {
				AnalyticsCard(
					slices = uiSlices,
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
				PrimaryActionButton(
					text = "Return to Dashboard",
					onClick = onReturnToDashboard
				)
			}
		}
	}
}

@Composable
private fun AnalyticsCard(
	slices: List<AnalyticsSliceUi>,
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
					color = Gunmetal,
					fontWeight = FontWeight.SemiBold
				)
				Box(
					modifier = Modifier
						.size(32.dp)
						.clip(CircleShape)
					.background(CardBone),
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
private fun AnalyticsChart(slices: List<AnalyticsSliceUi>) {
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
private fun LegendEntry(slice: AnalyticsSliceUi) {
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
					color = NightBlack,
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

private data class AnalyticsSliceUi(
	val label: String,
	val value: Float,
	val color: Color
)

private fun TransactionAnalyticsSlice.toUiSlice(): AnalyticsSliceUi {
	val color = when (category) {
		AnalyticsCategory.QUANTUM -> DeepBlue
		AnalyticsCategory.NORMAL -> Steel200
		AnalyticsCategory.INTERCEPTED -> AccentOrange
	}
	return AnalyticsSliceUi(label = label, value = value, color = color)
}

private data class SliceAngle(
	val slice: AnalyticsSliceUi,
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

