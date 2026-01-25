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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Warning
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.designsystem.components.QuantumTopBar
import com.example.quantumaccess.core.designsystem.theme.AccentOrange
import com.example.quantumaccess.core.designsystem.theme.AlertRed
import com.example.quantumaccess.core.designsystem.theme.CardBone
import com.example.quantumaccess.core.designsystem.theme.Cloud100
import com.example.quantumaccess.core.designsystem.theme.Cloud200
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Emerald
import com.example.quantumaccess.core.designsystem.theme.Gunmetal
import com.example.quantumaccess.core.designsystem.theme.MistBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.QuantumAccessTheme
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel200
import com.example.quantumaccess.core.designsystem.theme.Steel300
import com.example.quantumaccess.data.sample.RepositoryProvider
import com.example.quantumaccess.domain.model.AnalyticsCategory
import com.example.quantumaccess.domain.model.ComparisonTimelineStep
import com.example.quantumaccess.domain.model.TimelineStepStatus
import com.example.quantumaccess.domain.model.TransactionAnalyticsSlice
import com.example.quantumaccess.domain.repository.TransactionRepository
import java.util.Locale

import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import android.app.Activity
import com.example.quantumaccess.core.util.findActivity

import androidx.compose.foundation.layout.navigationBarsPadding

@Composable
fun AnalyticsDashboardScreen(
	modifier: Modifier = Modifier,
	onReturnToDashboard: () -> Unit = {},
	onLogout: () -> Unit = {},
	transactionRepository: TransactionRepository = RepositoryProvider.transactionRepository
) {
    // Force status bar icons to be light (visible on blue background)
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
            }
        }
    }

	val slices = remember(transactionRepository) { transactionRepository.getSecurityDistribution() }
	val uiSlices = remember(slices) { slices.map { it.toUiSlice() } }
	val timelineSteps = remember(transactionRepository) { transactionRepository.getComparisonTimelineSteps() }

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(Cloud100)
            .navigationBarsPadding()
	) {
		Column(modifier = Modifier.fillMaxSize()) {
			QuantumTopBar(
				title = "QuantumAccess",
				subtitle = "Quantum Security Analysis",
				showLogoutButton = true,
				onLogoutClick = onLogout
			)
			Column(
				modifier = Modifier
					.weight(1f)
					.fillMaxWidth()
					.padding(horizontal = 20.dp)
					.verticalScroll(rememberScrollState()),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				Spacer(modifier = Modifier.height(16.dp))
				
				// Distribution Chart
				AnalyticsCard(
					slices = uiSlices,
					modifier = Modifier
						.fillMaxWidth()
						.widthIn(max = 380.dp)
				)
				
				Spacer(modifier = Modifier.height(20.dp))
				
				// Why This Matters Panel
				WhyThisMattersPanel(
					modifier = Modifier.fillMaxWidth()
				)
				
				Spacer(modifier = Modifier.height(20.dp))
				
				// Comparison Timeline
				ComparisonTimeline(
					steps = timelineSteps,
					modifier = Modifier.fillMaxWidth()
				)
				
				Spacer(modifier = Modifier.height(16.dp))
			}
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.padding(horizontal = 20.dp, vertical = 16.dp)
			) {
				PrimaryActionButton(
					text = "Back to Dashboard",
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

// ===== Why This Matters Panel =====

@Composable
private fun WhyThisMattersPanel(
	modifier: Modifier = Modifier
) {
	Surface(
		shape = RoundedCornerShape(20.dp),
		color = Color.White,
		shadowElevation = 4.dp,
		modifier = modifier
	) {
		Column(
			modifier = Modifier.padding(20.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					modifier = Modifier
						.size(36.dp)
						.clip(CircleShape)
						.background(DeepBlue.copy(alpha = 0.1f)),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Filled.Lightbulb,
						contentDescription = null,
						tint = DeepBlue,
						modifier = Modifier.size(20.dp)
					)
				}
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = "Why This Matters",
					style = MaterialTheme.typography.titleMedium,
					color = NightBlack,
					fontWeight = FontWeight.SemiBold
				)
			}
			
			Spacer(modifier = Modifier.height(16.dp))
			
			WhyThisMattersBullet(
				icon = Icons.Filled.Security,
				iconColor = DeepBlue,
				text = "Datele medicale expuse pot afecta tratamentul și viața privată."
			)
			
			Spacer(modifier = Modifier.height(12.dp))
			
			WhyThisMattersBullet(
				icon = Icons.Filled.Shield,
				iconColor = SecureGreen,
				text = "Plățile compromise duc la pierderi financiare și fraudă."
			)
			
			Spacer(modifier = Modifier.height(12.dp))
			
			WhyThisMattersBullet(
				icon = Icons.Filled.Lock,
				iconColor = AccentOrange,
				text = "Trecerea la soluții QKD (Quantum Key Distribution) protejează datele pe termen lung."
			)
		}
	}
}

@Composable
private fun WhyThisMattersBullet(
	icon: ImageVector,
	iconColor: Color,
	text: String
) {
	Row(
		verticalAlignment = Alignment.Top
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = iconColor,
			modifier = Modifier.size(20.dp)
		)
		Spacer(modifier = Modifier.width(12.dp))
		Text(
			text = text,
			style = MaterialTheme.typography.bodyMedium,
			color = Slate800,
			modifier = Modifier.weight(1f)
		)
	}
}

// ===== Comparison Timeline =====

@Composable
private fun ComparisonTimeline(
	steps: List<ComparisonTimelineStep>,
	modifier: Modifier = Modifier
) {
	Surface(
		shape = RoundedCornerShape(20.dp),
		color = Color.White,
		shadowElevation = 4.dp,
		modifier = modifier
	) {
		Column(
			modifier = Modifier.padding(20.dp)
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically
			) {
				Box(
					modifier = Modifier
						.size(36.dp)
						.clip(CircleShape)
						.background(DeepBlue.copy(alpha = 0.1f)),
					contentAlignment = Alignment.Center
				) {
					Icon(
						imageVector = Icons.Filled.Timeline,
						contentDescription = null,
						tint = DeepBlue,
						modifier = Modifier.size(20.dp)
					)
				}
				Spacer(modifier = Modifier.width(12.dp))
				Text(
					text = "Comparison: Normal vs Quantum",
					style = MaterialTheme.typography.titleMedium,
					color = NightBlack,
					fontWeight = FontWeight.SemiBold
				)
			}
			
			Spacer(modifier = Modifier.height(16.dp))
			
			// Header
			Row(
				modifier = Modifier.fillMaxWidth()
			) {
				Text(
					text = "Step",
					style = MaterialTheme.typography.labelSmall,
					color = Steel300,
					modifier = Modifier.weight(1.2f)
				)
				Text(
					text = "Normal",
					style = MaterialTheme.typography.labelSmall,
					color = Steel300,
					modifier = Modifier.weight(1f)
				)
				Text(
					text = "Quantum",
					style = MaterialTheme.typography.labelSmall,
					color = Steel300,
					modifier = Modifier.weight(1f)
				)
			}
			
			Spacer(modifier = Modifier.height(8.dp))
			
			steps.forEachIndexed { index, step ->
				TimelineStepRow(step = step)
				if (index < steps.lastIndex) {
					Spacer(modifier = Modifier.height(8.dp))
				}
			}
		}
	}
}

@Composable
private fun TimelineStepRow(
	step: ComparisonTimelineStep
) {
	Row(
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(8.dp))
			.background(Cloud100)
			.padding(10.dp),
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = step.stepName,
			style = MaterialTheme.typography.bodySmall,
			color = NightBlack,
			fontWeight = FontWeight.Medium,
			modifier = Modifier.weight(1.2f)
		)
		TimelineStatusCell(
			status = step.normalStatus,
			detail = step.normalDetail,
			modifier = Modifier.weight(1f)
		)
		TimelineStatusCell(
			status = step.quantumStatus,
			detail = step.quantumDetail,
			modifier = Modifier.weight(1f)
		)
	}
}

@Composable
private fun TimelineStatusCell(
	status: TimelineStepStatus,
	detail: String,
	modifier: Modifier = Modifier
) {
	val (icon, color) = when (status) {
		TimelineStepStatus.OK -> Icons.Filled.CheckCircle to SecureGreen
		TimelineStepStatus.WARNING -> Icons.Filled.Warning to AccentOrange
		TimelineStepStatus.COMPROMISED -> Icons.Filled.Error to AlertRed
		TimelineStepStatus.PENDING -> Icons.Filled.Timeline to Steel300
	}
	
	Column(
		modifier = modifier,
		horizontalAlignment = Alignment.CenterHorizontally
	) {
		Icon(
			imageVector = icon,
			contentDescription = null,
			tint = color,
			modifier = Modifier.size(18.dp)
		)
		Spacer(modifier = Modifier.height(2.dp))
		Text(
			text = detail,
			style = MaterialTheme.typography.labelSmall,
			color = Slate800,
			maxLines = 2
		)
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun AnalyticsDashboardPreview() {
	QuantumAccessTheme {
		AnalyticsDashboardScreen()
	}
}
