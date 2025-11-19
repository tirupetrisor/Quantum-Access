package com.example.quantumaccess.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.statusBarsPadding
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
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Divider
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.ui.components.InputField
import com.example.quantumaccess.ui.components.QuantumLogoBlueOrbits
import com.example.quantumaccess.ui.theme.DeepBlue

enum class TransactionMode { NORMAL, QUANTUM }

@Composable
fun InitiateTransactionScreen(
	modifier: Modifier = Modifier,
	onContinue: (amount: String, beneficiary: String, mode: TransactionMode) -> Unit = { _, _, _ -> }
) {
	var amount by remember { mutableStateOf("") }
	var beneficiary by remember { mutableStateOf("") }
	var selectedMode by remember { mutableStateOf(TransactionMode.QUANTUM) }

	Column(
		modifier = modifier
			.fillMaxSize()
			.background(Color.White)
	) {
		HeaderBar()

		Column(
			modifier = Modifier
				.padding(horizontal = 16.dp, vertical = 12.dp)
				.fillMaxSize(),
			verticalArrangement = Arrangement.SpaceBetween
		) {
			Box(
				modifier = Modifier
					.fillMaxWidth()
					.weight(1f),
				contentAlignment = Alignment.Center
			) {
				Surface(
				shape = RoundedCornerShape(20.dp),
				shadowElevation = 10.dp,
				color = Color.White,
				modifier = Modifier
					.fillMaxWidth()
					.widthIn(max = 420.dp)
					.align(Alignment.Center),
				tonalElevation = 0.dp,
				border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF1F5F9))
				) {
				Column(
					modifier = Modifier
						.fillMaxWidth()
						.padding(20.dp),
					verticalArrangement = Arrangement.Top
				) {
					Spacer(modifier = Modifier.height(20.dp))
					Text(
						text = "Initiate Transaction",
						style = MaterialTheme.typography.titleLarge,
						color = DeepBlue,
						fontWeight = FontWeight.SemiBold,
						modifier = Modifier.fillMaxWidth(),
						textAlign = TextAlign.Center
					)

					Spacer(modifier = Modifier.height(20.dp))
					InputField(
						value = amount,
						onValueChange = { amount = it },
						label = "Amount (€)",
						placeholder = "0.00",
						labelIcon = Icons.Filled.Euro
					)
					Spacer(modifier = Modifier.height(24.dp))
					InputField(
						value = beneficiary,
						onValueChange = { beneficiary = it },
						label = "Beneficiary",
						placeholder = "Enter recipient name or account",
						labelIcon = Icons.Filled.Person
					)

					Spacer(modifier = Modifier.height(24.dp))
					Spacer(modifier = Modifier.height(32.dp))
					Text(
						text = "Select Transaction Mode",
						style = MaterialTheme.typography.titleLarge,
						color = DeepBlue,
						fontWeight = FontWeight.SemiBold,
						modifier = Modifier.fillMaxWidth(),
						textAlign = TextAlign.Center
					)
					Spacer(modifier = Modifier.height(16.dp))

					// Normal card
					ModeCard(
						title = "Normal Transaction",
						subtitle = "Standard secure processing",
						selected = selectedMode == TransactionMode.NORMAL,
						onClick = { selectedMode = TransactionMode.NORMAL },
						enabled = true
					)
					Spacer(modifier = Modifier.height(12.dp))
					// Quantum card
					ModeCard(
						title = "Quantum Transaction (QKD Mode)",
						subtitle = "Maximum quantum encryption",
						selected = selectedMode == TransactionMode.QUANTUM,
						onClick = { selectedMode = TransactionMode.QUANTUM },
						enabled = true
					)

					Spacer(modifier = Modifier.height(8.dp))
				}
				}
			}
			PrimaryButton(
				text = "Continue",
				enabled = amount.isNotBlank() && beneficiary.isNotBlank(),
				onClick = { onContinue(amount.trim(), beneficiary.trim(), selectedMode) }
			)
			Spacer(modifier = Modifier.height(8.dp))
		}
	}
}

@Composable
private fun HeaderBar() {
	Box(
		modifier = Modifier
			.fillMaxWidth()
			.statusBarsPadding()
			.background(DeepBlue)
			.padding(horizontal = 16.dp, vertical = 14.dp)
	) {
		Row(verticalAlignment = Alignment.CenterVertically) {
			Box(
				modifier = Modifier
					.size(32.dp)
					.clip(RoundedCornerShape(8.dp))
					.background(Color.White),
				contentAlignment = Alignment.Center
			) {
				QuantumLogoBlueOrbits(showText = false, iconSize = 20.dp)
			}
			Spacer(modifier = Modifier.size(8.dp))
			Text("QuantumAccess", color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
		}
	}
}

private data class ModeColors(
	val container: Color,
	val border: Color,
	val title: Color,
	val subtitle: Color
)

@Composable
private fun ModeCard(
	title: String,
	subtitle: String,
	selected: Boolean,
	onClick: () -> Unit,
	enabled: Boolean
) {
	val bg = if (selected) DeepBlue else Color.White
	val br = if (selected) DeepBlue else Color(0xFFE5E7EB)
	val titleColor = if (selected) Color.White else Color(0xFF111827)
	val subtitleColor = if (selected) Color(0xFFBFDBFE) else Color(0xFF6B7280)
	Surface(
		shape = RoundedCornerShape(14.dp),
		shadowElevation = 0.dp,
		color = bg,
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(14.dp))
			.border(2.dp, br, RoundedCornerShape(14.dp))
			.clickable(enabled = enabled) { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.fillMaxWidth()
				.padding(16.dp)
		) {
			Column {
				Text(text = title, style = MaterialTheme.typography.titleSmall, color = titleColor, fontWeight = FontWeight.Medium)
				Spacer(modifier = Modifier.height(4.dp))
				Text(text = subtitle, style = MaterialTheme.typography.bodySmall, color = subtitleColor)
			}
			if (selected) {
				Box(
					modifier = Modifier
						.size(22.dp)
						.clip(CircleShape)
						.background(Color.White),
					contentAlignment = Alignment.Center
				) {
					Box(
						modifier = Modifier
							.size(8.dp)
							.clip(CircleShape)
							.background(DeepBlue)
					)
				}
			} else {
				Box(
					modifier = Modifier
						.size(22.dp)
						.clip(CircleShape)
						.border(2.dp, Color(0xFFD1D5DB), CircleShape)
				)
			}
		}
	}
}

@Composable
private fun PrimaryButton(text: String, enabled: Boolean, onClick: () -> Unit) {
	Surface(
		shape = RoundedCornerShape(14.dp),
		shadowElevation = 0.dp,
		color = if (enabled) DeepBlue else Color(0xFF9CA3AF),
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(14.dp))
			.clickable(enabled = enabled) { onClick() }
	) {
		Box(
			modifier = Modifier.padding(vertical = 14.dp),
			contentAlignment = Alignment.Center
		) {
			Text(text = text, color = Color.White, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
		}
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewInitiate() {
	InitiateTransactionScreen()
}


