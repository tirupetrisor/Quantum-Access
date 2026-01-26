package com.example.quantumaccess.feature.transactions.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Euro
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.quantumaccess.core.designsystem.components.InputField
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.designsystem.components.QuantumTopBar
import com.example.quantumaccess.core.designsystem.theme.AccentOrange
import com.example.quantumaccess.core.designsystem.theme.BorderLight
import com.example.quantumaccess.core.designsystem.theme.CardBone
import com.example.quantumaccess.core.designsystem.theme.Cloud100
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.OutlineGray
import com.example.quantumaccess.core.designsystem.theme.Slate700
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel300
import com.example.quantumaccess.domain.model.TransactionScenario
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.Security
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.quantumaccess.core.designsystem.theme.AlertRed
import com.example.quantumaccess.core.util.findActivity
import com.example.quantumaccess.data.local.SecurePrefsManager

enum class TransactionMode { NORMAL, QUANTUM }

@Composable
fun InitiateTransactionScreen(
	modifier: Modifier = Modifier,
	onContinue: (
		amount: String?,
		beneficiary: String?,
		patientId: String?,
		accessReason: String?,
		mode: TransactionMode,
		scenario: TransactionScenario,
		simulateAttack: Boolean
	) -> Unit = { _, _, _, _, _, _, _ -> }
) {
	val context = LocalContext.current
	val prefs = remember { SecurePrefsManager(context) }
	
	var amount by remember { mutableStateOf("") }
	var beneficiary by remember { mutableStateOf("") }
	var patientId by remember { mutableStateOf("") }
	var accessReason by remember { mutableStateOf("") }
	var selectedMode by remember { mutableStateOf(TransactionMode.QUANTUM) }
	var selectedScenario by remember { mutableStateOf(TransactionScenario.BANKING_PAYMENT) }
	var simulateAttack by remember { mutableStateOf(false) }

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

    val isFormValid = if (selectedScenario == TransactionScenario.BANKING_PAYMENT) {
        amount.isNotBlank() && beneficiary.isNotBlank()
    } else {
        patientId.isNotBlank() && accessReason.isNotBlank()
    }

	Column(
		modifier = modifier
			.fillMaxSize()
			.background(Cloud100)
            .navigationBarsPadding()
	) {
		QuantumTopBar(
			title = "QuantumAccess",
			subtitle = "Initiate Transaction"
		)

		Column(
			modifier = Modifier
				.fillMaxSize()
                .verticalScroll(rememberScrollState())
				.padding(horizontal = 20.dp, vertical = 20.dp)
		) {
			// === SCENARIO SELECTOR ===
			SectionHeader(
				title = "Operation Type",
				subtitle = "Select transaction type"
			)
			Spacer(modifier = Modifier.height(12.dp))
			
			ScenarioCard(
				title = TransactionScenario.BANKING_PAYMENT.displayName,
				subtitle = TransactionScenario.BANKING_PAYMENT.description,
				selected = selectedScenario == TransactionScenario.BANKING_PAYMENT,
				onClick = { selectedScenario = TransactionScenario.BANKING_PAYMENT }
			)
			Spacer(modifier = Modifier.height(10.dp))
			ScenarioCard(
				title = TransactionScenario.MEDICAL_RECORD_ACCESS.displayName,
				subtitle = TransactionScenario.MEDICAL_RECORD_ACCESS.description,
				selected = selectedScenario == TransactionScenario.MEDICAL_RECORD_ACCESS,
				onClick = { selectedScenario = TransactionScenario.MEDICAL_RECORD_ACCESS }
			)
			
			Spacer(modifier = Modifier.height(24.dp))
			
			// === CÂMPURI DIFERITE PER SCENARIU ===
			Surface(
				shape = RoundedCornerShape(16.dp),
				color = Color.White,
				shadowElevation = 2.dp,
				modifier = Modifier.fillMaxWidth()
			) {
				Column(
					modifier = Modifier.padding(20.dp)
				) {
					if (selectedScenario == TransactionScenario.BANKING_PAYMENT) {
						// Banking payment: Amount + Beneficiary
						SectionHeader(
							title = "Payment Details",
							subtitle = "Complete transfer information"
						)
						Spacer(modifier = Modifier.height(16.dp))
						InputField(
							value = amount,
							onValueChange = { amount = it },
							label = "Amount (€)",
							placeholder = "0.00",
							labelIcon = Icons.Filled.Euro
						)
						Spacer(modifier = Modifier.height(16.dp))
						InputField(
							value = beneficiary,
							onValueChange = { beneficiary = it },
							label = "Beneficiary",
							placeholder = "Enter name or account",
							labelIcon = Icons.Filled.Person
						)
					} else {
						// Medical record access: Patient ID + Access reason
						SectionHeader(
							title = "Medical Access Details",
							subtitle = "Complete access information"
						)
						Spacer(modifier = Modifier.height(16.dp))
						InputField(
							value = patientId,
							onValueChange = { patientId = it },
							label = "Patient ID / SSN",
							placeholder = "Enter patient ID or SSN",
							labelIcon = Icons.Filled.Person
						)
						Spacer(modifier = Modifier.height(16.dp))
						InputField(
							value = accessReason,
							onValueChange = { accessReason = it },
							label = "Access Reason",
							placeholder = "Ex: Consultation, Emergency, Check-up",
							labelIcon = Icons.Filled.Person
						)
					}
				}
			}
			
			Spacer(modifier = Modifier.height(24.dp))
			
			// === PROCESSING MODE ===
			SectionHeader(
				title = "Processing Mode",
				subtitle = "Choose security level"
			)
			Spacer(modifier = Modifier.height(12.dp))

			// Normal card
			ModeCard(
				title = "Normal Transaction",
				subtitle = "Processing with standard cryptography",
				selected = selectedMode == TransactionMode.NORMAL,
				onClick = { selectedMode = TransactionMode.NORMAL },
				enabled = true
			)
			Spacer(modifier = Modifier.height(10.dp))
			// Quantum card
			ModeCard(
				title = "Quantum Transaction (QKD)",
				subtitle = "Maximum security with quantum algorithms",
				selected = selectedMode == TransactionMode.QUANTUM,
				onClick = { selectedMode = TransactionMode.QUANTUM },
				enabled = true
			)
			
			Spacer(modifier = Modifier.height(24.dp))
			
			// === SIMULATE ATTACK TOGGLE ===
			SimulateAttackToggle(
				checked = simulateAttack,
				onCheckedChange = { simulateAttack = it }
			)

			Spacer(modifier = Modifier.height(32.dp))
			
			// === CONTINUE BUTTON ===
			PrimaryActionButton(
				text = "Continue",
				enabled = isFormValid,
				onClick = {
					val amt = if (selectedScenario == TransactionScenario.BANKING_PAYMENT) amount.trim().takeIf { it.isNotBlank() } else null
					val ben = if (selectedScenario == TransactionScenario.BANKING_PAYMENT) beneficiary.trim().takeIf { it.isNotBlank() } else null
					val pat = if (selectedScenario == TransactionScenario.MEDICAL_RECORD_ACCESS) patientId.trim().takeIf { it.isNotBlank() } else null
					val reason = if (selectedScenario == TransactionScenario.MEDICAL_RECORD_ACCESS) accessReason.trim().takeIf { it.isNotBlank() } else null
					onContinue(amt, ben, pat, reason, selectedMode, selectedScenario, simulateAttack)
				}
			)
			Spacer(modifier = Modifier.height(20.dp))
		}
	}
}

@Composable
private fun SectionHeader(
	title: String,
	subtitle: String? = null
) {
	Column {
		Text(
			text = title,
			style = MaterialTheme.typography.titleMedium,
			color = NightBlack,
			fontWeight = FontWeight.SemiBold
		)
		if (subtitle != null) {
			Spacer(modifier = Modifier.height(4.dp))
			Text(
				text = subtitle,
				style = MaterialTheme.typography.bodySmall,
				color = Steel300
			)
		}
	}
}

@Composable
private fun ScenarioCard(
	title: String,
	subtitle: String,
	selected: Boolean,
	onClick: () -> Unit
) {
	// Când este selectat: fundal gri, border gri, doar bulina albastră
	val bg = if (selected) CardBone else Color.White
	val br = if (selected) Slate700.copy(alpha = 0.3f) else BorderLight
	val titleColor = NightBlack // Păstrăm negru pentru ambele stări
	val borderWidth = if (selected) 2.dp else 1.dp
	
	Surface(
		shape = RoundedCornerShape(16.dp),
		color = bg,
		shadowElevation = if (selected) 4.dp else 1.dp,
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.border(borderWidth, br, RoundedCornerShape(16.dp))
			.clickable { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.fillMaxWidth()
				.padding(18.dp)
		) {
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = title, 
					style = MaterialTheme.typography.bodyLarge, 
					color = titleColor, 
					fontWeight = FontWeight.SemiBold
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = subtitle, 
					style = MaterialTheme.typography.bodySmall, 
					color = Steel300
				)
			}
			Spacer(modifier = Modifier.width(12.dp))
			if (selected) {
				// Doar bulina este albastră când este selectat
				Box(
					modifier = Modifier
						.size(24.dp)
						.clip(CircleShape)
						.background(DeepBlue),
					contentAlignment = Alignment.Center
				) {
					Box(
						modifier = Modifier
							.size(10.dp)
							.clip(CircleShape)
							.background(Color.White)
					)
				}
			} else {
				Box(
					modifier = Modifier
						.size(24.dp)
						.clip(CircleShape)
						.border(2.dp, OutlineGray, CircleShape)
				)
			}
		}
	}
}

@Composable
private fun SimulateAttackToggle(
	checked: Boolean,
	onCheckedChange: (Boolean) -> Unit
) {
	Surface(
		shape = RoundedCornerShape(16.dp),
		color = if (checked) AccentOrange else Color.White,
		shadowElevation = 2.dp,
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			modifier = Modifier
				.fillMaxWidth()
				.padding(horizontal = 18.dp, vertical = 16.dp)
		) {
			Box(
				modifier = Modifier
					.size(40.dp)
					.clip(CircleShape)
					.background(
						if (checked) Color.White.copy(alpha = 0.2f) 
						else Steel300.copy(alpha = 0.1f)
					),
				contentAlignment = Alignment.Center
			) {
				Icon(
					imageVector = Icons.Filled.Warning,
					contentDescription = null,
					tint = if (checked) Color.White else Steel300,
					modifier = Modifier.size(22.dp)
				)
			}
			Spacer(modifier = Modifier.width(14.dp))
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = "Simulate quantum attack (demo)",
					style = MaterialTheme.typography.bodyMedium,
					color = if (checked) Color.White else NightBlack,
					fontWeight = FontWeight.SemiBold
				)
				Spacer(modifier = Modifier.height(2.dp))
				Text(
					text = "Test the difference between modes",
					style = MaterialTheme.typography.bodySmall,
					color = if (checked) Color.White.copy(alpha = 0.9f) else Steel300
				)
			}
			Spacer(modifier = Modifier.width(8.dp))
			Switch(
				checked = checked,
				onCheckedChange = onCheckedChange,
				colors = SwitchDefaults.colors(
					checkedThumbColor = Color.White,
					checkedTrackColor = Color.White.copy(alpha = 0.5f),
					uncheckedThumbColor = Color.White,
					uncheckedTrackColor = Steel300
				)
			)
		}
	}
}

@Composable
private fun ModeCard(
	title: String,
	subtitle: String,
	selected: Boolean,
	onClick: () -> Unit,
	enabled: Boolean
) {
	val bg = if (selected) DeepBlue else Color.White
	val br = if (selected) DeepBlue.copy(alpha = 0.3f) else BorderLight
	val titleColor = if (selected) Color.White else NightBlack
	val subtitleColor = if (selected) Color(0xFFBFDBFE) else Slate800
	val borderWidth = if (selected) 2.dp else 1.dp
	
	Surface(
		shape = RoundedCornerShape(16.dp),
		shadowElevation = if (selected) 6.dp else 2.dp,
		color = bg,
		modifier = Modifier
			.fillMaxWidth()
			.clip(RoundedCornerShape(16.dp))
			.border(borderWidth, br, RoundedCornerShape(16.dp))
			.clickable(enabled = enabled) { onClick() }
	) {
		Row(
			verticalAlignment = Alignment.CenterVertically,
			horizontalArrangement = Arrangement.SpaceBetween,
			modifier = Modifier
				.fillMaxWidth()
				.padding(18.dp)
		) {
			Column(modifier = Modifier.weight(1f)) {
				Text(
					text = title, 
					style = MaterialTheme.typography.bodyLarge, 
					color = titleColor, 
					fontWeight = FontWeight.SemiBold
				)
				Spacer(modifier = Modifier.height(4.dp))
				Text(
					text = subtitle, 
					style = MaterialTheme.typography.bodySmall, 
					color = subtitleColor
				)
			}
			Spacer(modifier = Modifier.width(12.dp))
			if (selected) {
				Box(
					modifier = Modifier
						.size(24.dp)
						.clip(CircleShape)
						.background(Color.White),
					contentAlignment = Alignment.Center
				) {
					Box(
						modifier = Modifier
							.size(10.dp)
							.clip(CircleShape)
							.background(DeepBlue)
					)
				}
			} else {
				Box(
					modifier = Modifier
						.size(24.dp)
						.clip(CircleShape)
						.border(2.dp, OutlineGray, CircleShape)
				)
			}
		}
	}
}

@Composable
private fun EveSimulationToggle(
	isEnabled: Boolean,
	onToggle: (Boolean) -> Unit
) {
	Surface(
		shape = RoundedCornerShape(12.dp),
		color = if (isEnabled) AlertRed.copy(alpha = 0.1f) else Color(0xFFF5F5F5),
		border = BorderStroke(1.dp, if (isEnabled) AlertRed.copy(alpha = 0.3f) else BorderLight),
		modifier = Modifier.fillMaxWidth()
	) {
		Row(
			modifier = Modifier
				.fillMaxWidth()
				.padding(12.dp),
			horizontalArrangement = Arrangement.SpaceBetween,
			verticalAlignment = Alignment.CenterVertically
		) {
			Row(
				verticalAlignment = Alignment.CenterVertically,
				modifier = Modifier.weight(1f)
			) {
				Icon(
					imageVector = Icons.Filled.Security,
					contentDescription = null,
					tint = if (isEnabled) AlertRed else Color.Gray,
					modifier = Modifier.size(20.dp)
				)
				Spacer(modifier = Modifier.width(10.dp))
				Column {
					Text(
						text = "Eve Simulation",
						style = MaterialTheme.typography.bodyMedium,
						fontWeight = FontWeight.SemiBold,
						color = if (isEnabled) AlertRed else NightBlack
					)
					Text(
						text = if (isEnabled) "Eavesdropping simulation active" else "Test interception detection",
						style = MaterialTheme.typography.bodySmall,
						color = Color.Gray
					)
				}
			}
			Switch(
				checked = isEnabled,
				onCheckedChange = onToggle,
				colors = SwitchDefaults.colors(
					checkedThumbColor = Color.White,
					checkedTrackColor = AlertRed,
					uncheckedThumbColor = Color.White,
					uncheckedTrackColor = Color.Gray.copy(alpha = 0.4f)
				)
			)
		}
	}
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun PreviewInitiate() {
	InitiateTransactionScreen()
}
