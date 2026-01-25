package com.example.quantumaccess.feature.transactions.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.quantumaccess.core.designsystem.theme.AlertRed
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel300
import com.example.quantumaccess.domain.model.TransactionChannel
import com.example.quantumaccess.domain.model.TransactionResult
import com.example.quantumaccess.domain.model.TransactionScenario

/**
 * Overlay afișat imediat după fiecare tranzacție.
 * Arată titlu, explicație și buton pentru detalii.
 */
@Composable
fun TransactionResultOverlay(
    visible: Boolean,
    transactionResult: TransactionResult?,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = visible && transactionResult != null,
        enter = fadeIn() + slideInVertically { it },
        exit = fadeOut() + slideOutVertically { it }
    ) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
        ) {
            transactionResult?.let { result ->
                OverlayCard(
                    result = result,
                    onDismiss = onDismiss,
                    onViewDetails = onViewDetails
                )
            }
        }
    }
}

@Composable
private fun OverlayCard(
    result: TransactionResult,
    onDismiss: () -> Unit,
    onViewDetails: () -> Unit
) {
    val isQuantum = result.mode == TransactionChannel.QUANTUM
    val isCompromised = result.compromised
    val eveDetected = result.eveDetected
    
    // Determine overlay content based on scenario and result
    val overlayContent = getOverlayContent(
        scenario = result.scenario,
        isQuantum = isQuantum,
        isCompromised = isCompromised,
        eveDetected = eveDetected
    )
    
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 16.dp,
        modifier = Modifier
            .padding(24.dp)
            .fillMaxWidth()
            .clickable(enabled = false) { } // Prevent click-through
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Close button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Close",
                        tint = Steel300
                    )
                }
            }
            
            // Icon
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(overlayContent.iconBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = overlayContent.icon,
                    contentDescription = null,
                    tint = overlayContent.iconColor,
                    modifier = Modifier.size(36.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Title
            Text(
                text = overlayContent.title,
                style = MaterialTheme.typography.titleLarge,
                color = overlayContent.titleColor,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // Description
            Text(
                text = overlayContent.description,
                style = MaterialTheme.typography.bodyMedium,
                color = Slate800,
                textAlign = TextAlign.Center,
                lineHeight = MaterialTheme.typography.bodyMedium.lineHeight * 1.3
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Security scores
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ScoreBadge(
                    label = "Normal Score",
                    score = result.normalScore,
                    isHighlighted = !isQuantum
                )
                ScoreBadge(
                    label = "Quantum Score",
                    score = result.quantumScore,
                    isHighlighted = isQuantum
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // View details button
            Button(
                onClick = onViewDetails,
                colors = ButtonDefaults.buttonColors(
                    containerColor = DeepBlue
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "View Details",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun ScoreBadge(
    label: String,
    score: Int,
    isHighlighted: Boolean
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = Steel300
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = if (isHighlighted) DeepBlue.copy(alpha = 0.1f) else Color(0xFFF5F5F5)
        ) {
            Text(
                text = "$score/100",
                style = MaterialTheme.typography.titleMedium,
                color = if (isHighlighted) DeepBlue else NightBlack,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}

private data class OverlayContent(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val iconColor: Color,
    val iconBackgroundColor: Color,
    val title: String,
    val titleColor: Color,
    val description: String
)

private fun getOverlayContent(
    scenario: TransactionScenario,
    isQuantum: Boolean,
    isCompromised: Boolean,
    eveDetected: Boolean
): OverlayContent {
    return when {
        // BANKING_PAYMENT + Normal + compromised
        isCompromised && scenario == TransactionScenario.BANKING_PAYMENT -> OverlayContent(
            icon = Icons.Filled.Error,
            iconColor = AlertRed,
            iconBackgroundColor = AlertRed.copy(alpha = 0.1f),
            title = "Tranzacție compromisă",
            titleColor = AlertRed,
            description = "Tranzacția ta a fost compromisă în simulare. Datele bancare ar fi putut fi expuse. Folosirea QKD (Quantum Key Distribution) reduce acest risc."
        )
        // MEDICAL_RECORD_ACCESS + Normal + compromised
        isCompromised && scenario == TransactionScenario.MEDICAL_RECORD_ACCESS -> OverlayContent(
            icon = Icons.Filled.Error,
            iconColor = AlertRed,
            iconBackgroundColor = AlertRed.copy(alpha = 0.1f),
            title = "Acces compromis la dosarul medical",
            titleColor = AlertRed,
            description = "În simulare, dosarul medical ar fi putut fi expus. Informațiile sensibile riscă impact major."
        )
        // BANKING_PAYMENT + Quantum + safe (with or without Eve detected)
        isQuantum && scenario == TransactionScenario.BANKING_PAYMENT -> OverlayContent(
            icon = Icons.Filled.Shield,
            iconColor = SecureGreen,
            iconBackgroundColor = SecureGreen.copy(alpha = 0.1f),
            title = "Tranzacție protejată",
            titleColor = SecureGreen,
            description = "Tranzacția a fost protejată de un canal QKD securizat. Atacurile simulate nu au reușit să compromită datele."
        )
        // MEDICAL_RECORD_ACCESS + Quantum + safe (with or without Eve detected)
        isQuantum && scenario == TransactionScenario.MEDICAL_RECORD_ACCESS -> OverlayContent(
            icon = Icons.Filled.Shield,
            iconColor = SecureGreen,
            iconBackgroundColor = SecureGreen.copy(alpha = 0.1f),
            title = "Acces medical securizat",
            titleColor = SecureGreen,
            description = "Canalul QKD a detectat tentativa de interceptare și a regenerat cheile — informațiile rămân protejate."
        )
        // Default: Normal mode without attack
        else -> OverlayContent(
            icon = Icons.Filled.Info,
            iconColor = DeepBlue,
            iconBackgroundColor = DeepBlue.copy(alpha = 0.1f),
            title = "Procesare standard",
            titleColor = NightBlack,
            description = "${scenario.displayName} a fost procesată cu criptografie standard. Pentru protecție maximă împotriva amenințărilor viitoare, recomandăm modul Quantum."
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TransactionResultOverlayPreview() {
    TransactionResultOverlay(
        visible = true,
        transactionResult = TransactionResult(
            transactionId = "test-123",
            scenario = TransactionScenario.BANKING_PAYMENT,
            mode = TransactionChannel.QUANTUM,
            normalScore = 72,
            quantumScore = 95,
            qber = 0.03,
            eveDetected = true,
            compromised = false,
            success = true,
            message = "Atac detectat și blocat"
        ),
        onDismiss = {},
        onViewDetails = {}
    )
}
