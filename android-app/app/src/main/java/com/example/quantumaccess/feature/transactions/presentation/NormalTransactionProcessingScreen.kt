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
import com.example.quantumaccess.domain.repository.TransactionRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale

private const val PROCESS_DURATION_MS = 4500

@Composable
fun NormalTransactionProcessingScreen(
    modifier: Modifier = Modifier,
    amount: String = "€1,250.00",
    beneficiary: String = "John D. – Quantum Savings",
    onReturnToDashboard: () -> Unit = {},
    onLogout: () -> Unit = {},
    transactionRepository: TransactionRepository = RepositoryProvider.transactionRepository
) {
    var isProcessing by remember { mutableStateOf(true) }
    var transactionSaved by remember { mutableStateOf(false) } // Prevent duplicates
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = PROCESS_DURATION_MS, easing = FastOutSlowInEasing)
        )
        
        if (!transactionSaved) {
            // Save transaction to DB
            // Parse amount string to Double (remove currency symbol and commas)
            val cleanAmount = amount.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
            
            transactionRepository.insertTransaction(
                amount = cleanAmount,
                mode = "NORMAL",
                status = "COMPLETED",
                intercepted = false, // Server will update this status if interception occurred
                beneficiary = beneficiary
            )
            transactionSaved = true
        }
        
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
                    progress = progress.value,
                    isProcessing = isProcessing
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
    progress: Float,
    isProcessing: Boolean
) {
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
                color = Slate800
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = "To",
                style = MaterialTheme.typography.bodySmall,
                color = Steel200
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = beneficiary,
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
                    color = Steel300
                )
    }
}

@Preview(showSystemUi = true)
@Composable
private fun NormalTransactionProcessingPreview() {
    NormalTransactionProcessingScreen()
}
