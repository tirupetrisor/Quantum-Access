package com.example.quantumaccess.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.ui.components.HeaderLogoutButton
import com.example.quantumaccess.ui.components.QuantumLogoBlueOrbits
import com.example.quantumaccess.ui.theme.DeepBlue
import com.example.quantumaccess.ui.theme.SecureGreen
import kotlinx.coroutines.delay

private const val PROCESS_DURATION_MS = 4500

@Composable
fun NormalTransactionProcessingScreen(
	modifier: Modifier = Modifier,
	amount: String = "€1,250.00",
	beneficiary: String = "John D. – Quantum Savings",
	onReturnToDashboard: () -> Unit = {},
	onLogout: () -> Unit = {}
) {
	var isProcessing by remember { mutableStateOf(true) }
	var showLogoutDialog by remember { mutableStateOf(false) }
	val progress = remember { Animatable(0f) }

	LaunchedEffect(Unit) {
		progress.snapTo(0f)
		progress.animateTo(
			targetValue = 1f,
			animationSpec = tween(durationMillis = PROCESS_DURATION_MS, easing = FastOutSlowInEasing)
		)
		delay(300)
		isProcessing = false
	}

	Box(
		modifier = modifier
			.fillMaxSize()
			.background(Color.White)
	) {
		Column(
			modifier = Modifier.fillMaxSize()
		) {
			ProcessingHeader(onLogout = { showLogoutDialog = true })
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f)
					.padding(horizontal = 16.dp)
					.padding(top = 16.dp),
				contentAlignment = Alignment.Center
			) {
				NormalProcessingCard(
					amount = amount,
					beneficiary = beneficiary,
					progress = progress.value,
					isProcessing = isProcessing
				)
			}
			if (!isProcessing) {
				Spacer(modifier = Modifier.height(8.dp))
				ReturnButton(
					onClick = onReturnToDashboard,
					modifier = Modifier
						.fillMaxWidth()
						.padding(horizontal = 32.dp, vertical = 24.dp)
				)
			} else {
				Spacer(modifier = Modifier.height(32.dp))
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
private fun ProcessingHeader(onLogout: () -> Unit) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.statusBarsPadding()
			.background(DeepBlue)
			.padding(horizontal = 16.dp, vertical = 16.dp)
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
				Spacer(modifier = Modifier.size(12.dp))
				Column {
					Text(
						text = "QuantumAccess",
						color = Color.White,
						style = MaterialTheme.typography.titleMedium,
						fontWeight = FontWeight.SemiBold
					)
					Text(
						text = "Classical Transaction Processing",
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
private fun NormalProcessingCard(
	amount: String,
	beneficiary: String,
	progress: Float,
	isProcessing: Boolean
) {
	Surface(
		shape = RoundedCornerShape(28.dp),
		color = Color.White,
		shadowElevation = 12.dp,
		tonalElevation = 0.dp,
		border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
		modifier = Modifier
			.fillMaxWidth()
			.widthIn(max = 420.dp)
	) {
		Column(
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 24.dp, vertical = 28.dp),
			horizontalAlignment = Alignment.CenterHorizontally
		) {
			Text(
				text = amount,
				style = MaterialTheme.typography.headlineLarge,
				color = DeepBlue,
				fontWeight = FontWeight.Bold,
				textAlign = TextAlign.Center
			)
			Spacer(modifier = Modifier.height(8.dp))
			Text(
				text = "Transfer Amount",
				style = MaterialTheme.typography.bodyMedium,
				color = Color(0xFF757575)
			)
			Spacer(modifier = Modifier.height(28.dp))
			Text(
				text = "To",
				style = MaterialTheme.typography.bodySmall,
				color = Color(0xFF9CA3AF)
			)
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = beneficiary,
				style = MaterialTheme.typography.titleMedium,
				color = Color(0xFF111827),
				fontWeight = FontWeight.SemiBold
			)
			Spacer(modifier = Modifier.height(20.dp))
			Divider(color = Color(0xFFE0E0E0))
			Spacer(modifier = Modifier.height(24.dp))

			AnimatedVisibility(
				visible = isProcessing,
				enter = fadeIn(),
				exit = fadeOut()
			) {
				ProcessingSection(progress = progress)
			}

			AnimatedVisibility(
				visible = !isProcessing,
				enter = fadeIn(),
				exit = fadeOut()
			) {
				SuccessSection()
			}
		}
	}
}

@Composable
private fun ProcessingSection(progress: Float) {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		ProcessingProgress(progress = progress)
		Spacer(modifier = Modifier.height(16.dp))
		Text(
			text = "Processing classical transaction...",
			style = MaterialTheme.typography.bodyMedium,
			color = Color(0xFF757575),
			textAlign = TextAlign.Center
		)
	}
}

@Composable
private fun ProcessingProgress(progress: Float) {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.height(10.dp)
			.clip(CircleShape)
			.background(Color(0xFFF1F5F9))
	) {
		Box(
			modifier = Modifier
				.fillMaxHeight()
				.fillMaxWidth(progress.coerceIn(0f, 1f))
				.background(
					Brush.horizontalGradient(
						colors = listOf(Color(0xFF4B5563), Color(0xFFC0C0C0))
					)
				)
		)
	}
}

@Composable
private fun SuccessSection() {
	Column(horizontalAlignment = Alignment.CenterHorizontally) {
		Box(
			modifier = Modifier
				.size(72.dp)
				.clip(CircleShape)
				.background(Color(0xFFE8F9EF)),
			contentAlignment = Alignment.Center
		) {
			Icon(
				imageVector = Icons.Rounded.Check,
				contentDescription = null,
				tint = SecureGreen,
				modifier = Modifier.size(34.dp)
			)
		}
		Spacer(modifier = Modifier.height(16.dp))
		Row(verticalAlignment = Alignment.CenterVertically) {
			Box(
				modifier = Modifier
					.size(22.dp)
					.clip(RoundedCornerShape(6.dp))
					.background(SecureGreen.copy(alpha = 0.15f)),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.Rounded.Check,
					contentDescription = null,
					tint = SecureGreen,
					modifier = Modifier.size(16.dp)
				)
			}
			Spacer(modifier = Modifier.size(8.dp))
			Text(
				text = "Transaction successful",
				style = MaterialTheme.typography.titleMedium,
				color = SecureGreen,
				fontWeight = FontWeight.SemiBold
			)
		}
		Spacer(modifier = Modifier.height(6.dp))
		Text(
			text = "Interception undetected",
			style = MaterialTheme.typography.bodySmall,
			color = Color(0xFF94A3B8)
		)
	}
}

@Composable
private fun ReturnButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
	Surface(
		shape = RoundedCornerShape(18.dp),
		color = DeepBlue,
		modifier = modifier
	) {
		Box(
			modifier = Modifier
				.fillMaxWidth()
				.clickable(onClick = onClick)
				.padding(vertical = 14.dp),
			contentAlignment = Alignment.Center
		) {
			Text(
				text = "Return to Dashboard",
				color = Color.White,
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
			.background(Color.Black.copy(alpha = 0.5f)),
		contentAlignment = Alignment.Center
	) {
		Surface(
			shape = RoundedCornerShape(16.dp),
			shadowElevation = 10.dp,
			color = Color.White,
			modifier = Modifier
				.padding(horizontal = 20.dp)
		) {
			Column(modifier = Modifier.padding(16.dp)) {
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
				Spacer(modifier = Modifier.height(16.dp))
				Row(
					horizontalArrangement = Arrangement.spacedBy(12.dp),
					modifier = Modifier.fillMaxWidth()
				) {
					Surface(
						shape = RoundedCornerShape(12.dp),
						color = Color(0xFFF3F4F6),
						modifier = Modifier
							.weight(1f)
							.clip(RoundedCornerShape(12.dp))
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
						shape = RoundedCornerShape(12.dp),
						color = DeepBlue,
						modifier = Modifier
							.weight(1f)
							.clip(RoundedCornerShape(12.dp))
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

@Preview(showSystemUi = true)
@Composable
private fun NormalTransactionProcessingPreview() {
	NormalTransactionProcessingScreen()
}

