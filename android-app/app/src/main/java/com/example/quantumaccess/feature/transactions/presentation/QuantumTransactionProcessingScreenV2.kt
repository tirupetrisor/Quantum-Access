package com.example.quantumaccess.feature.transactions.presentation

import android.util.Log
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.quantumaccess.app.QuantumAccessApplication
import com.example.quantumaccess.core.designsystem.components.QuantumLogoGradientBadge
import com.example.quantumaccess.core.designsystem.components.QuantumTopBar
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import com.example.quantumaccess.core.util.findActivity
import com.example.quantumaccess.data.sample.RepositoryProvider
import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.viewmodel.QuantumTransactionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * New Quantum Transaction Processing Screen with REAL QKD integration
 */
@Composable
fun QuantumTransactionProcessingScreenV2(
    modifier: Modifier = Modifier,
    amount: String = "€2,450.00",
    beneficiary: String = "TechCorp Solutions SRL",
    quantumId: String = "#QTX-7F2A-8B91",
    onReturnToDashboard: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    val context = LocalContext.current
    val app = context.applicationContext as QuantumAccessApplication
    val viewModel: QuantumTransactionViewModel = viewModel(
        factory = QuantumTransactionViewModel.Factory(app)
    )
    
    // Force status bar icons to be light
    val view = LocalView.current
    if (!view.isInEditMode) {
        androidx.compose.runtime.SideEffect {
            val window = view.context.findActivity()?.window
            if (window != null) {
                val controller = WindowCompat.getInsetsController(window, view)
                controller.isAppearanceLightStatusBars = false
            }
        }
    }

    var currentStep by remember { mutableStateOf<QuantumProcessStep?>(null) }
    var isCompleted by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    val progress = remember { Animatable(0f) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        progress.snapTo(0f)
        
        // Parse amount
        val cleanAmount = amount.replace("[^\\d.]".toRegex(), "").toDoubleOrNull() ?: 0.0
        
        // Process quantum transaction with real QKD
        viewModel.processQuantumTransaction(
            amount = cleanAmount,
            beneficiary = beneficiary
        ) { step ->
            currentStep = step
            scope.launch {
                progress.animateTo(
                    step.progress,
                    animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
                )
            }
            
            if (step.isTerminal) {
                delay(400)
                isCompleted = true
            } else {
                delay(800)
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0E1549), Color(0xFF1A237E))
                )
            )
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            QuantumTopBar(
                title = "QuantumAccess",
                subtitle = "Real QKD Transaction Mode",
                backgroundColor = Color(0xFF151E68),
                showLogoutButton = true,
                onLogoutClick = onLogout,
                leadingContent = {
                    QuantumLogoGradientBadge(
                        size = 44.dp,
                        gradientColors = listOf(Color(0xFF1A237E), Color(0xFF3F51B5)),
                        iconScale = 0.55f
                    )
                }
            )

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
                        currentStep = currentStep ?: QuantumProcessStep(
                            0f,
                            "Initializing...",
                            "Starting quantum protocol",
                            false
                        )
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
                text = "Real QKD Service Active",
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
                text = "Quantum Key Distribution Active",
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
    currentStep: QuantumProcessStep
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
private fun QuantumStatusMessage(step: QuantumProcessStep) {
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
                color = com.example.quantumaccess.core.designsystem.theme.DeepBlue,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
