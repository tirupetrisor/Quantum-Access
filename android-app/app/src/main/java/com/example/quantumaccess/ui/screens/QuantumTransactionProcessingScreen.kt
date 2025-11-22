package com.example.quantumaccess.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.ui.components.HeaderLogoutButton
import com.example.quantumaccess.ui.theme.DeepBlue
import com.example.quantumaccess.ui.theme.SecureGreen
import kotlinx.coroutines.delay

@Composable
fun QuantumTransactionProcessingScreen(
	modifier: Modifier = Modifier,
	amount: String = "€2,450.00",
	beneficiary: String = "TechCorp Solutions SRL",
	quantumId: String = "#QTX-7F2A-8B91",
	onReturnToDashboard: () -> Unit = {},
	onLogout: () -> Unit = {}
) {
	val steps = remember {
		listOf(
			QuantumStep(
				progress = 0.25f,
				status = "Establishing quantum-secured channel...",
				detail = "Initializing quantum key distribution"
			),
			QuantumStep(
				progress = 0.5f,
				status = "Quantum entanglement verified...",
				detail = "Generating cryptographic keys"
			),
			QuantumStep(
				progress = 0.75f,
				status = "Transmitting via QKD protocol...",
				detail = "Quantum entanglement verified"
			),
			QuantumStep(
				progress = 1f,
				status = "Quantum-secured transaction complete",
				detail = "Transaction verified and confirmed",
				isTerminal = true
			)
		)
	}

	var currentStepIndex by remember { mutableIntStateOf(0) }
	var isCompleted by remember { mutableStateOf(false) }
	var showLogoutDialog by remember { mutableStateOf(false) }
	val progress = remember { Animatable(0f) }

	LaunchedEffect(Unit) {
		progress.snapTo(0f)
		steps.forEachIndexed { index, step ->
			currentStepIndex = index
			progress.animateTo(
				step.progress,
				animationSpec = tween(durationMillis = 1400, easing = FastOutSlowInEasing)
			)
			if (step.isTerminal) {
				delay(400)
				isCompleted = true
			} else {
				delay(900)
			}
		}
	}

	val currentStep = steps[currentStepIndex.coerceIn(0, steps.lastIndex)]

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(
				Brush.verticalGradient(
					colors = listOf(Color(0xFF0E1549), Color(0xFF1A237E))
				)
			)
	) {
		Column(
			modifier = Modifier
				.fillMaxSize()
		) {
			QuantumHeader(onLogout = { showLogoutDialog = true })

			Column(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
					.padding(horizontal = 20.dp)
					.padding(top = 24.dp),
				horizontalAlignment = Alignment.CenterHorizontally
			) {
				ConnectionStatusSection()
				Spacer(modifier = Modifier.height(24.dp))
				Box(
					modifier = Modifier
						.fillMaxWidth()
						.weight(1f),
					contentAlignment = Alignment.Center
				) {
					QuantumTransactionCard(
						amount = amount,
						beneficiary = beneficiary,
						quantumId = quantumId,
						progress = progress.value,
						currentStep = currentStep
					)
				}
			}

			if (isCompleted) {
				QuantumReturnButton(
					onClick = onReturnToDashboard,
					modifier = Modifier
						.padding(horizontal = 24.dp, vertical = 20.dp)
				)
			} else {
				Spacer(modifier = Modifier.height(30.dp))
			}
		}

		if (showLogoutDialog) {
			LogoutDialog(
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
private fun QuantumHeader(onLogout: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.statusBarsPadding()
			.background(Color(0xFF151E68))
			.padding(horizontal = 20.dp, vertical = 18.dp)
	) {
		Row(
			modifier = Modifier.fillMaxWidth(),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(verticalAlignment = Alignment.CenterVertically) {
				GradientAtomLogo()
				Spacer(modifier = Modifier.width(12.dp))
				Column {
					Text(
						text = "QuantumAccess",
						color = Color.White,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold
					)
					Text(
						text = "Quantum Transaction Mode",
						color = Color(0xFF9FB2FF),
						style = MaterialTheme.typography.labelSmall
					)
				}
			}
			HeaderLogoutButton(onClick = onLogout)
		}
	}
}

@Composable
private fun ConnectionStatusSection() {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Box(
				modifier = Modifier
					.size(10.dp)
					.clip(CircleShape)
					.background(SecureGreen)
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = "Connected to RonaQCI Timișoara",
				color = Color(0xFFDAE4FF),
				style = MaterialTheme.typography.bodyMedium
			)
		}
		Spacer(modifier = Modifier.height(10.dp))
		QuantumSecureBadge()
	}
}

@Composable
private fun QuantumSecureBadge() {
	Surface(
		color = Color.White.copy(alpha = 0.05f),
		shape = RoundedCornerShape(50),
		border = BorderStroke(1.dp, Color(0xFF57E1DE).copy(alpha = 0.4f))
	) {
		Row(
			modifier = Modifier
				.padding(horizontal = 14.dp, vertical = 8.dp),
			verticalAlignment = Alignment.CenterVertically
		) {
			Icon(
				imageVector = Icons.Filled.Security,
				contentDescription = null,
				tint = Color(0xFF57E1DE),
				modifier = Modifier.size(18.dp)
			)
			Spacer(modifier = Modifier.width(8.dp))
			Text(
				text = "Quantum-Secured Channel Active",
				color = Color(0xFF9DEAE9),
				style = MaterialTheme.typography.labelMedium,
				fontWeight = FontWeight.Medium
			)
		}
	}
}

@Composable
private fun QuantumTransactionCard(
	amount: String,
	beneficiary: String,
	quantumId: String,
	progress: Float,
	currentStep: QuantumStep
) {
	Surface(
		shape = RoundedCornerShape(28.dp),
		color = Color(0xFF202E80).copy(alpha = 0.9f),
		tonalElevation = 0.dp,
		shadowElevation = 18.dp,
		border = BorderStroke(1.dp, Color.White.copy(alpha = 0.08f)),
		modifier = Modifier
			.fillMaxWidth()
			.widthIn(max = 420.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp, vertical = 28.dp)
		) {
			TransactionDetailRow(label = "Amount", value = amount, emphasize = true)
			Spacer(modifier = Modifier.height(14.dp))
			TransactionDetailRow(label = "Beneficiary", value = beneficiary)
			Spacer(modifier = Modifier.height(14.dp))
			TransactionDetailRow(label = "Quantum ID", value = quantumId, monospaced = true)
			Spacer(modifier = Modifier.height(24.dp))
			QuantumProgressBar(progress = progress)
			Spacer(modifier = Modifier.height(24.dp))
			QuantumStatusMessage(step = currentStep)
		}
	}
}

@Composable
private fun TransactionDetailRow(
	label: String,
	value: String,
	emphasize: Boolean = false,
	monospaced: Boolean = false
) {
	Row(
		modifier = Modifier.fillMaxWidth(),
		horizontalArrangement = Arrangement.SpaceBetween,
		verticalAlignment = Alignment.CenterVertically
	) {
		Text(
			text = label,
			color = Color(0xFFAEC3FF),
			style = MaterialTheme.typography.bodySmall
		)
		Text(
			text = value,
			color = if (emphasize) Color.White else Color(0xFFE4ECFF),
			style = if (emphasize) MaterialTheme.typography.titleLarge else MaterialTheme.typography.bodyMedium,
			fontWeight = if (emphasize) FontWeight.SemiBold else FontWeight.Medium,
			fontSize = if (emphasize) 24.sp else MaterialTheme.typography.bodyMedium.fontSize,
			textAlign = TextAlign.End,
			fontFamily = if (monospaced) FontFamily.Monospace else FontFamily.Default
		)
	}
}

@Composable
private fun QuantumProgressBar(progress: Float) {
	Column {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.height(8.dp)
				.clip(RoundedCornerShape(50))
				.background(Color(0xFF152059))
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth(progress.coerceIn(0f, 1f))
					.fillMaxHeight()
					.background(
						Brush.horizontalGradient(
							colors = listOf(Color(0xFF6ED0FF), Color(0xFFD5E4FF))
						)
					)
			)
		}
	}
}

@Composable
private fun QuantumStatusMessage(step: QuantumStep) {
	if (step.isTerminal) {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			Box(
				modifier = Modifier
					.size(36.dp)
					.clip(RoundedCornerShape(10.dp))
					.background(SecureGreen.copy(alpha = 0.2f)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.Rounded.Check,
					contentDescription = null,
					tint = SecureGreen,
					modifier = Modifier.size(20.dp)
				)
			}
			Spacer(modifier = Modifier.height(10.dp))
			Text(
				text = step.status,
				color = Color.White,
				style = MaterialTheme.typography.bodyMedium,
				fontWeight = FontWeight.SemiBold,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth()
			)
			Spacer(modifier = Modifier.height(6.dp))
			Text(
				text = step.detail,
				color = Color(0xFFA8FFDE),
				style = MaterialTheme.typography.bodySmall,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth()
			)
		}
	} else {
		Column(horizontalAlignment = Alignment.CenterHorizontally) {
			CircularProgressIndicator(
				strokeWidth = 2.dp,
				color = Color(0xFF8AB8FF),
				modifier = Modifier
					.size(20.dp)
					.alpha(0.9f)
			)
			Spacer(modifier = Modifier.height(12.dp))
			Text(
				text = step.status,
				color = Color(0xFFE3EBFF),
				style = MaterialTheme.typography.bodyMedium,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth()
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = step.detail,
				color = Color(0xFFB3C6FF),
				style = MaterialTheme.typography.bodySmall,
				fontWeight = FontWeight.Medium,
				textAlign = TextAlign.Center,
				modifier = Modifier.fillMaxWidth()
			)
		}
	}
}

@Composable
private fun QuantumReturnButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
	Surface(
		shape = RoundedCornerShape(24.dp),
		color = Color.White,
		shadowElevation = 12.dp,
		border = BorderStroke(1.dp, Color(0xFFD8E0FF)),
		modifier = modifier
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clickable(onClick = onClick)
				.padding(vertical = 18.dp),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "Return to Dashboard",
				color = DeepBlue,
				style = MaterialTheme.typography.titleMedium,
				fontWeight = FontWeight.SemiBold
			)
		}
	}
}

@Composable
private fun LogoutDialog(onCancel: () -> Unit, onConfirm: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxSize()
			.background(Color.Black.copy(alpha = 0.55f)),
		contentAlignment = Alignment.Center
	) {
		Surface(
			shape = RoundedCornerShape(18.dp),
			shadowElevation = 12.dp,
			color = Color.White,
			modifier = Modifier.padding(horizontal = 24.dp)
		) {
			Column(modifier = Modifier.padding(18.dp)) {
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
							.clickable { onCancel() }
					) {
						Box(
							modifier = Modifier.padding(vertical = 12.dp),
							contentAlignment = Alignment.Center
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
							.clickable { onConfirm() }
					) {
						Box(
							modifier = Modifier.padding(vertical = 12.dp),
							contentAlignment = Alignment.Center
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

private data class QuantumStep(
	val progress: Float,
	val status: String,
	val detail: String,
	val isTerminal: Boolean = false
)

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun QuantumTransactionProcessingPreview() {
	QuantumTransactionProcessingScreen()
}

@Composable
private fun GradientAtomLogo() {
	Box(
		modifier = Modifier
			.size(44.dp)
			.clip(RoundedCornerShape(12.dp))
			.background(
				Brush.linearGradient(
					colors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5))
				)
			),
		contentAlignment = Alignment.Center
	) {
		AtomGlyph(modifier = Modifier.size(22.dp))
	}
}

@Composable
private fun AtomGlyph(modifier: Modifier = Modifier) {
	Canvas(modifier = modifier) {
		val s = size.minDimension / 80f
		val center = Offset(size.width / 2f, size.height / 2f)
		drawCircle(color = Color.White, radius = 4f * s, center = center)
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
