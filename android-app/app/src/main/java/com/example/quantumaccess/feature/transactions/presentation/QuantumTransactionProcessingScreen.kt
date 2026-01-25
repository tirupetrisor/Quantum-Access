package com.example.quantumaccess.feature.transactions.presentation

import android.util.Log
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.rotate
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.components.QuantumLogoGradientBadge
import com.example.quantumaccess.core.designsystem.components.QuantumTopBar
import com.example.quantumaccess.core.designsystem.theme.AlertRed
import com.example.quantumaccess.core.designsystem.theme.SecureGreen
import com.example.quantumaccess.data.sample.RepositoryProvider
import com.example.quantumaccess.domain.model.QuantumProcessStep
import com.example.quantumaccess.domain.model.TransactionChannel
import com.example.quantumaccess.domain.model.TransactionRequest
import com.example.quantumaccess.domain.model.TransactionResult
import com.example.quantumaccess.domain.model.TransactionScenario
import com.example.quantumaccess.domain.repository.TransactionRepository
import kotlinx.coroutines.delay

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.example.quantumaccess.core.util.findActivity

private const val QUANTUM_TRANSACTION_TAG = "QuantumTransactionScreen"

@Composable
fun QuantumTransactionProcessingScreen(
    modifier: Modifier = Modifier,
    amount: String = "0",
    beneficiary: String = "",
    patientId: String = "",
    accessReason: String = "",
    quantumId: String = "#QTX-7F2A-8B91",
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

    val steps = remember(transactionRepository) { transactionRepository.getQuantumProcessSteps() }.takeIf { it.isNotEmpty() }
        ?: listOf(
            QuantumProcessStep(
                progress = 1f,
                status = "Quantum transaction status unavailable",
                detail = "No steps defined",
                isTerminal = true
            )
        )

    var currentStepIndex by remember { mutableIntStateOf(0) }
    var isCompleted by remember { mutableStateOf(false) }
    var transactionSaved by remember { mutableStateOf(false) }
    var transactionResult by remember { mutableStateOf<TransactionResult?>(null) }
    val progress = remember { Animatable(0f) }
    
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
        steps.forEachIndexed { index, step ->
            currentStepIndex = index
            
            // Longer animation for each step to see the quantum channel properly
            progress.animateTo(
                step.progress,
                animationSpec = tween(durationMillis = 2000, easing = FastOutSlowInEasing)
            )
            
            if (step.isTerminal) {
                delay(800)
                isCompleted = true
                
                if (!transactionSaved) {
                    val isMedical = transactionScenario == TransactionScenario.MEDICAL_RECORD_ACCESS
                    val cleanAmount = amount.replace("[^\\d.]".toRegex(), "").toDoubleOrNull()?.takeIf { it > 0 }
                    val request = TransactionRequest(
                        amount = if (isMedical) null else (cleanAmount ?: 0.0),
                        beneficiary = if (isMedical) null else beneficiary.takeIf { it.isNotBlank() },
                        patientId = if (isMedical) patientId.takeIf { it.isNotBlank() } else null,
                        accessReason = if (isMedical) accessReason.takeIf { it.isNotBlank() } else null,
                        scenario = transactionScenario,
                        mode = TransactionChannel.QUANTUM,
                        simulateAttack = simulateAttack
                    )

                    val result = transactionRepository.processTransaction(request)
                    if (result.isSuccess) {
                        transactionSaved = true
                        transactionResult = result.getOrNull()
                    } else {
                        Log.e(
                            QUANTUM_TRANSACTION_TAG,
                            "Failed to persist quantum transaction",
                            result.exceptionOrNull()
                        )
                    }
                }
                
            } else {
                // Longer delay between steps to see animation properly
                delay(1800)
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
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            QuantumTopBar(
                title = "QuantumAccess",
                subtitle = "Quantum Transaction Mode",
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
                    val isMedical = transactionScenario == TransactionScenario.MEDICAL_RECORD_ACCESS
                    val dispAmount = if (isMedical) "Medical Access" else amount
                    val dispBen = if (isMedical) {
                        listOfNotNull(
                            patientId.takeIf { it.isNotBlank() }?.let { "Patient: $it" },
                            accessReason.takeIf { it.isNotBlank() }?.let { "Reason: $it" }
                        ).joinToString(" | ").ifEmpty { "—" }
                    } else beneficiary.ifBlank { "—" }
                    QuantumTransactionCard(
                        amount = dispAmount,
                        beneficiary = dispBen,
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
    currentStep: QuantumProcessStep
) {
    // Track photon/qubit count - increases throughout the entire quantum process
    var photonCount by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(currentStep) {
        // Keep counting qubits as long as we're processing
        if (!currentStep.isTerminal) {
            while (photonCount < 256) { // Up to 256 qubits for 256-bit key
                photonCount++
                delay(120) // Steady increase
            }
        }
    }
    
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
            Spacer(modifier = Modifier.height(20.dp))
            
            // Live Quantum Channel Visualization with QBER - ALWAYS visible during processing
            if (!currentStep.isTerminal) {
                LiveQuantumChannelVisualizer(
                    currentStep = currentStep,
                    photonCount = photonCount,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
            } else {
                // Show final QBER result when completed
                FinalQBERResultCard(currentStep = currentStep)
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            QuantumProgressBar(progress = progress)
            Spacer(modifier = Modifier.height(20.dp))
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
        // Detect if it's a failure/error/abort
        val isFailure = step.status.contains("Failed", ignoreCase = true) ||
                       step.status.contains("Error", ignoreCase = true) ||
                       step.status.contains("Aborted", ignoreCase = true) ||
                       step.status.contains("Detected", ignoreCase = true) // Eve detected
        
        val iconColor = if (isFailure) AlertRed else SecureGreen
        val iconBg = if (isFailure) AlertRed.copy(alpha = 0.2f) else SecureGreen.copy(alpha = 0.2f)
        val icon = if (isFailure) Icons.Filled.Close else Icons.Rounded.Check
        val textColor = if (isFailure) Color(0xFFFFB4AB) else Color(0xFFA8FFDE)
        
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconColor,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = step.status,
                color = if (isFailure) AlertRed.copy(alpha = 0.9f) else Color.White,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = step.detail,
                color = textColor,
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

/**
 * Live Quantum Channel Visualizer - Shows animated photon transmission
 * with Alice, Bob, Eve detection and real-time QBER display
 */
@Composable
private fun LiveQuantumChannelVisualizer(
    currentStep: QuantumProcessStep,
    photonCount: Int,
    modifier: Modifier = Modifier
) {
    // Detect if Eve is present based on step status
    val isEveDetectionStep = currentStep.status.contains("Eve", ignoreCase = true) ||
                             currentStep.status.contains("Eavesdropping", ignoreCase = true) ||
                             currentStep.status.contains("Scanning", ignoreCase = true)
    val isEveDetected = currentStep.status.contains("Detected", ignoreCase = true) &&
                        isEveDetectionStep
    
    // Extract QBER from detail if available
    val qberValue = remember(currentStep.detail) {
        val qberMatch = Regex("QBER[:\\s]*([\\d.]+)%?").find(currentStep.detail)
        qberMatch?.groupValues?.get(1)?.toDoubleOrNull()?.let { 
            if (it > 1) it / 100 else it 
        } ?: if (isEveDetected) 0.15 else 0.03
    }
    
    // Animation for pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    // Photon animation
    val photonProgress by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "photonProgress"
    )
    
    Column(
        modifier = modifier
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF152059).copy(alpha = 0.7f),
                        Color(0xFF1A237E).copy(alpha = 0.5f)
                    )
                ),
                RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        // Title row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (isEveDetectionStep) "🔍 Eve Detection" else "🔬 BB84 Protocol",
                color = if (isEveDetected) Color(0xFFFF6B6B) else Color(0xFF00D9FF),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            )
            
            // Photon counter badge
            Surface(
                color = Color(0xFF00D9FF).copy(alpha = 0.15f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "$photonCount qubits",
                    color = Color(0xFF00D9FF),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 11.sp
                )
            }
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Canvas for quantum channel animation
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(90.dp)
        ) {
            val canvasWidth = size.width
            val canvasHeight = size.height
            
            // Alice position (left)
            val aliceX = 45f
            val aliceY = canvasHeight / 2
            
            // Eve position (middle)
            val eveX = canvasWidth / 2
            val eveY = canvasHeight / 2
            
            // Bob position (right)
            val bobX = canvasWidth - 45f
            val bobY = canvasHeight / 2
            
            // Draw quantum channel line
            drawLine(
                color = Color(0xFF2A3F5F),
                start = Offset(aliceX, aliceY),
                end = Offset(bobX, bobY),
                strokeWidth = 2f
            )
            
            // Draw Alice (sender) - cyan
            drawCircle(
                color = Color(0xFF00D9FF),
                radius = 22f,
                center = Offset(aliceX, aliceY),
                alpha = pulseAlpha * 0.4f
            )
            drawCircle(
                color = Color(0xFF00D9FF),
                radius = 16f,
                center = Offset(aliceX, aliceY)
            )
            
            // Draw Eve (if detection step) - red/orange
            if (isEveDetectionStep) {
                val eveColor = if (isEveDetected) Color(0xFFFF3D71) else Color(0xFFFFA726)
                val eveAlpha = if (isEveDetected) 0.9f else pulseAlpha * 0.6f
                
                drawCircle(
                    color = eveColor,
                    radius = 22f,
                    center = Offset(eveX, eveY),
                    alpha = eveAlpha * 0.4f
                )
                drawCircle(
                    color = eveColor,
                    radius = 16f,
                    center = Offset(eveX, eveY),
                    alpha = eveAlpha
                )
            }
            
            // Draw Bob (receiver) - green
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 22f,
                center = Offset(bobX, bobY),
                alpha = pulseAlpha * 0.4f
            )
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = 16f,
                center = Offset(bobX, bobY)
            )
            
            // Draw photons in transit (5 photons at different positions)
            for (i in 0..4) {
                val offset = (i * 0.2f + photonProgress) % 1f
                val photonX = aliceX + (bobX - aliceX) * offset
                val photonY = aliceY
                
                // Alternate colors: blue for + basis, purple for × basis
                val photonColor = if (i % 2 == 0) Color(0xFF00D9FF) else Color(0xFFBB86FC)
                
                // Draw photon glow
                drawCircle(
                    color = photonColor,
                    radius = 10f,
                    center = Offset(photonX, photonY),
                    alpha = 0.3f
                )
                
                // Draw photon core
                drawCircle(
                    color = photonColor,
                    radius = 6f,
                    center = Offset(photonX, photonY),
                    alpha = 0.9f
                )
                
                // Draw polarization indicator
                val rotation = if (i % 2 == 0) 0f else 45f
                rotate(
                    degrees = rotation,
                    pivot = Offset(photonX, photonY)
                ) {
                    drawLine(
                        color = Color.White,
                        start = Offset(photonX - 4f, photonY),
                        end = Offset(photonX + 4f, photonY),
                        strokeWidth = 2f,
                        alpha = 0.9f
                    )
                }
                
                // Show Eve interception effect
                if (isEveDetectionStep && offset > 0.4f && offset < 0.6f) {
                    drawCircle(
                        color = Color(0xFFFF3D71),
                        radius = 14f,
                        center = Offset(photonX, photonY),
                        alpha = if (isEveDetected) 0.5f else 0.2f
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(10.dp))
        
        // Labels row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Alice",
                color = Color(0xFF00D9FF),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
            if (isEveDetectionStep) {
                Text(
                    text = "Eve",
                    color = if (isEveDetected) Color(0xFFFF6B6B) else Color(0xFFFFA726),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "Bob",
                color = Color(0xFF4CAF50),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // QBER Status Card
        Surface(
            color = if (isEveDetected) 
                Color(0xFFFF3D71).copy(alpha = 0.2f) 
            else 
                Color(0xFF4CAF50).copy(alpha = 0.15f),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = if (isEveDetected) "⚠️ Eavesdropping Detected!" else "✅ Channel Secure",
                        color = if (isEveDetected) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                    Text(
                        text = if (isEveDetected) 
                            "Attack detected - regenerating keys" 
                        else 
                            "No interception detected",
                        color = if (isEveDetected) 
                            Color(0xFFFF6B6B).copy(alpha = 0.7f) 
                        else 
                            Color(0xFF4CAF50).copy(alpha = 0.7f),
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 10.sp
                    )
                }
                
                // QBER Badge
                Surface(
                    color = if (isEveDetected) 
                        Color(0xFFFF3D71).copy(alpha = 0.3f)
                    else 
                        Color(0xFF4CAF50).copy(alpha = 0.25f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = "QBER: ${String.format("%.1f", qberValue * 100)}%",
                        color = if (isEveDetected) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.ExtraBold,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

/**
 * Final QBER Result Card - Shows at the end of transaction
 */
@Composable
private fun FinalQBERResultCard(
    currentStep: QuantumProcessStep
) {
    // Extract QBER from detail
    val qberValue = remember(currentStep.detail) {
        val qberMatch = Regex("QBER[:\\s]*([\\d.]+)%").find(currentStep.detail)
        qberMatch?.groupValues?.get(1)?.toDoubleOrNull() ?: 3.0
    }
    
    val isEveDetected = currentStep.status.contains("Attack", ignoreCase = true) ||
                        currentStep.status.contains("Blocked", ignoreCase = true) ||
                        qberValue > 11.0
    
    Surface(
        color = if (isEveDetected) 
            Color(0xFFFF3D71).copy(alpha = 0.15f) 
        else 
            Color(0xFF4CAF50).copy(alpha = 0.12f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(
            1.dp, 
            if (isEveDetected) Color(0xFFFF3D71).copy(alpha = 0.3f) 
            else Color(0xFF4CAF50).copy(alpha = 0.3f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "📊 Quantum Security Analysis",
                color = Color.White,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // QBER Indicator
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "QBER",
                        color = Color(0xFFB3C6FF),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "${String.format("%.1f", qberValue)}%",
                        color = if (qberValue > 11) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (qberValue > 11) "Above threshold" else "Safe",
                        color = if (qberValue > 11) Color(0xFFFF6B6B) else Color(0xFF4CAF50),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
                
                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(Color(0xFF3A4A7A))
                )
                
                // Security Status
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Status",
                        color = Color(0xFFB3C6FF),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = if (isEveDetected) "🛡️" else "✅",
                        fontSize = 28.sp
                    )
                    Text(
                        text = if (isEveDetected) "Attack Blocked" else "Secure",
                        color = if (isEveDetected) Color(0xFFFFA726) else Color(0xFF4CAF50),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold
                    )
                }
                
                // Vertical divider
                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(50.dp)
                        .background(Color(0xFF3A4A7A))
                )
                
                // Protocol
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Protocol",
                        color = Color(0xFFB3C6FF),
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        text = "BB84",
                        color = Color(0xFF00D9FF),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "QKD",
                        color = Color(0xFF00D9FF),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}

@Preview(showSystemUi = true, showBackground = true)
@Composable
private fun QuantumTransactionProcessingPreview() {
    QuantumTransactionProcessingScreen()
}
