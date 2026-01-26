package com.example.quantumaccess.feature.transactions.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import android.util.Log
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Warning
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
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.components.PrimaryActionButton
import com.example.quantumaccess.core.designsystem.components.QuantumTopBar
import com.example.quantumaccess.core.designsystem.theme.BorderLight
import com.example.quantumaccess.core.designsystem.theme.BorderMuted
import com.example.quantumaccess.core.designsystem.theme.BorderSubtle
import com.example.quantumaccess.core.designsystem.theme.CardBone
import com.example.quantumaccess.core.designsystem.theme.Cloud250
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import com.example.quantumaccess.core.designsystem.theme.Silver
import com.example.quantumaccess.core.designsystem.theme.Slate700
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel200
import com.example.quantumaccess.core.designsystem.theme.Steel300
import com.example.quantumaccess.data.sample.RepositoryProvider
import com.example.quantumaccess.domain.model.TransactionChannel
import com.example.quantumaccess.domain.model.TransactionRequest
import com.example.quantumaccess.domain.model.TransactionResult
import com.example.quantumaccess.domain.model.TransactionScenario
import com.example.quantumaccess.domain.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.quantumaccess.core.util.findActivity

private const val PROCESS_DURATION_MS = 4500
private const val NORMAL_TRANSACTION_TAG = "NormalTransactionScreen"

@Composable
fun NormalTransactionProcessingScreen(
    modifier: Modifier = Modifier,
    amount: String = "0",
    beneficiary: String = "",
    patientId: String = "",
    accessReason: String = "",
    scenario: String = "BANKING_PAYMENT",
    simulateAttack: Boolean = false,
    onReturnToDashboard: () -> Unit = {},
    onLogout: () -> Unit = {},
    transactionRepository: TransactionRepository = RepositoryProvider.transactionRepository
) {
    // Force status bar icons to be light
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

    var isProcessing by remember { mutableStateOf(true) }
    var transactionSaved by remember { mutableStateOf(false) }
    var transactionResult by remember { mutableStateOf<TransactionResult?>(null) }
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()
    
    // Parse scenario
    val transactionScenario = remember(scenario) {
        when (scenario) {
            "MEDICAL_RECORD_ACCESS" -> TransactionScenario.MEDICAL_RECORD_ACCESS
            else -> TransactionScenario.BANKING_PAYMENT
        }
    }

    LaunchedEffect(Unit) {
        // Set simulate attack flag in repository
        transactionRepository.simulateAttackEnabled = simulateAttack
        
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = PROCESS_DURATION_MS, easing = FastOutSlowInEasing)
        )
        
        if (!transactionSaved) {
            val isMedical = transactionScenario == TransactionScenario.MEDICAL_RECORD_ACCESS
            val cleanAmount = amount.replace("[^\\d.]".toRegex(), "").toDoubleOrNull()?.takeIf { it > 0 }
            val request = TransactionRequest(
                amount = if (isMedical) null else (cleanAmount ?: 0.0),
                beneficiary = if (isMedical) null else beneficiary.takeIf { it.isNotBlank() },
                patientId = if (isMedical) patientId.takeIf { it.isNotBlank() } else null,
                accessReason = if (isMedical) accessReason.takeIf { it.isNotBlank() } else null,
                scenario = transactionScenario,
                mode = TransactionChannel.NORMAL,
                simulateAttack = simulateAttack
            )

            val result = transactionRepository.processTransaction(request)
            if (result.isSuccess) {
                transactionSaved = true
                transactionResult = result.getOrNull()
            } else {
                Log.e(
                    NORMAL_TRANSACTION_TAG,
                    "Failed to persist normal transaction",
                    result.exceptionOrNull()
                )
            }
        }
        
        delay(300)
        isProcessing = false
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            QuantumTopBar(
                title = "QuantumAccess",
                subtitle = "Classical Transaction Processing",
                showLogoutButton = true,
                onLogoutClick = onLogout
            )
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
                    patientId = patientId,
                    accessReason = accessReason,
                    scenario = transactionScenario,
                    progress = progress.value,
                    isProcessing = isProcessing,
                    transactionResult = transactionResult
                )
            }
            if (!isProcessing) {
                Spacer(modifier = Modifier.height(8.dp))
                PrimaryActionButton(
                    text = "Return to Dashboard",
                    onClick = onReturnToDashboard,
                    modifier = Modifier
                        .padding(horizontal = 32.dp, vertical = 24.dp)
                )
            } else {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }
}

@Composable
private fun NormalProcessingCard(
    amount: String,
    beneficiary: String,
    patientId: String,
    accessReason: String,
    scenario: TransactionScenario,
    progress: Float,
    isProcessing: Boolean,
    transactionResult: TransactionResult?
) {
    val isMedical = scenario == TransactionScenario.MEDICAL_RECORD_ACCESS
    val displayAmount = if (isMedical) "Medical Access" else amount
    val displayBeneficiary = if (isMedical) {
        listOfNotNull(
            patientId.takeIf { it.isNotBlank() }?.let { "Patient: $it" },
            accessReason.takeIf { it.isNotBlank() }?.let { "Reason: $it" }
        ).joinToString(" | ").ifEmpty { "—" }
    } else beneficiary.ifBlank { "—" }
    Surface(
        shape = RoundedCornerShape(28.dp),
        color = Color.White,
        shadowElevation = 12.dp,
        tonalElevation = 0.dp,
        border = BorderStroke(1.dp, BorderMuted),
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
            // Scenario badge
            Surface(
                shape = RoundedCornerShape(8.dp),
                color = CardBone,
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Text(
                    text = scenario.displayName,
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate700,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
            
            Text(
                text = displayAmount,
                style = MaterialTheme.typography.headlineLarge,
                color = DeepBlue,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            if (!isMedical) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Transfer Amount",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Slate800
                )
            }
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = if (isMedical) "Details" else "To",
                style = MaterialTheme.typography.bodySmall,
                color = Steel200
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = displayBeneficiary,
                style = MaterialTheme.typography.titleMedium,
                color = NightBlack,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = BorderSubtle)
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
                ResultSection(transactionResult = transactionResult)
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
                color = Slate800,
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
                    .background(CardBone)
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress.coerceIn(0f, 1f))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(Slate700, Silver)
                    )
                )
        )
    }
}

@Composable
private fun ResultSection(transactionResult: TransactionResult?) {
    val isCompromised = transactionResult?.compromised == true
    
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(if (isCompromised) Color(0xFFFDECEC) else Color(0xFFE8F9EF)),
            contentAlignment = Alignment.Center
        ) {
            if (isCompromised) {
                Icon(
                    imageVector = Icons.Rounded.Warning,
                    contentDescription = null,
                    tint = com.example.quantumaccess.core.designsystem.theme.AlertRed,
                    modifier = Modifier.size(34.dp)
                )
            } else {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = null,
                    tint = SecureGreen,
                    modifier = Modifier.size(34.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (isCompromised) {
            Text(
                text = "Security Alert: Data May Be Compromised",
                style = MaterialTheme.typography.titleMedium,
                color = com.example.quantumaccess.core.designsystem.theme.AlertRed,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        } else {
            Text(
                text = "Transaction Successful",
                style = MaterialTheme.typography.titleMedium,
                color = SecureGreen,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        // Additional warning section for compromised transactions
        if (isCompromised && transactionResult != null) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = com.example.quantumaccess.core.designsystem.theme.AlertRed.copy(alpha = 0.1f),
                border = BorderStroke(1.dp, com.example.quantumaccess.core.designsystem.theme.AlertRed.copy(alpha = 0.3f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Standard encryption can detect data modification, but cannot detect if data was intercepted (read without modification). Quantum mode can detect interception attempts.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate800,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
        
        // Security scores - only for non-compromised transactions
        if (transactionResult != null && !isCompromised) {
            Spacer(modifier = Modifier.height(16.dp))
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Your Transaction Score",
                    style = MaterialTheme.typography.labelSmall,
                    color = Steel300,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "${transactionResult.normalScore}/100",
                    style = MaterialTheme.typography.titleLarge,
                    color = DeepBlue,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
            }
        }
    }
}

@Composable
private fun ScoreIndicator(
    label: String,
    score: Int,
    isHighlighted: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = if (isHighlighted) Slate800 else Steel300,
            fontWeight = if (isHighlighted) FontWeight.SemiBold else FontWeight.Medium
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = "$score/100",
            style = MaterialTheme.typography.titleLarge,
            color = if (isHighlighted) DeepBlue else Steel300,
            fontWeight = FontWeight.Bold,
            fontSize = if (isHighlighted) 28.sp else 20.sp
        )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun NormalTransactionProcessingPreview() {
    NormalTransactionProcessingScreen()
}
