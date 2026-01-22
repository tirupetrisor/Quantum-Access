package com.example.quantumaccess.feature.quantum.presentation

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

/**
 * Live Quantum Channel Visualizer
 * 
 * Shows real-time BB84 protocol visualization:
 * - Photon transmission from Alice to Bob
 * - Polarization states (+ and × basis)
 * - Eve's interception attempts
 * - QBER calculation and monitoring
 * - Basis reconciliation process
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantumChannelVisualizerScreen(
    onBack: () -> Unit
) {
    var isTransmitting by remember { mutableStateOf(false) }
    var eveEnabled by remember { mutableStateOf(false) }
    var photons by remember { mutableStateOf<List<Photon>>(emptyList()) }
    var transmittedBits by remember { mutableStateOf(0) }
    var corruptedBits by remember { mutableStateOf(0) }
    var qber by remember { mutableStateOf(0.0) }
    var detectedEve by remember { mutableStateOf(false) }
    
    // Photon transmission simulation
    LaunchedEffect(isTransmitting) {
        if (isTransmitting) {
            while (isActive && isTransmitting) {
                // Generate new photon
                val newPhoton = Photon(
                    id = transmittedBits,
                    aliceBasis = if (Random.nextBoolean()) Basis.RECTILINEAR else Basis.DIAGONAL,
                    aliceBit = Random.nextBoolean(),
                    bobBasis = if (Random.nextBoolean()) Basis.RECTILINEAR else Basis.DIAGONAL,
                    progress = 0f,
                    interceptedByEve = eveEnabled && Random.nextDouble() < 0.5
                )
                
                photons = photons + newPhoton
                transmittedBits++
                
                // Simulate photon travel
                delay(800)
                
                // Update photon progress
                for (i in 0..20) {
                    val updatedPhotons = photons.map { photon ->
                        if (photon.id == newPhoton.id) {
                            photon.copy(progress = i / 20f)
                        } else photon
                    }
                    photons = updatedPhotons
                    delay(40)
                }
                
                // Check if corrupted
                val isCorrupted = newPhoton.interceptedByEve && 
                    newPhoton.aliceBasis == newPhoton.bobBasis &&
                    Random.nextBoolean()
                
                if (isCorrupted) {
                    corruptedBits++
                }
                
                // Calculate QBER
                if (transmittedBits > 0) {
                    qber = corruptedBits.toDouble() / transmittedBits
                    detectedEve = qber > 0.11
                }
                
                // Remove old photons
                photons = photons.filter { it.id >= transmittedBits - 5 }
                
                delay(200)
            }
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Quantum Channel Visualizer",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color(0xFF0A0E27)
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF0A0E27))
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Title and description
            Text(
                "BB84 Protocol Live Simulation",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00D9FF)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                "Watch photons travel through quantum channel in real-time",
                fontSize = 14.sp,
                color = Color(0xFF8E9AB3)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Main visualization canvas
            QuantumChannelCanvas(
                photons = photons,
                eveEnabled = eveEnabled,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Statistics cards
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatCard(
                    title = "Photons",
                    value = transmittedBits.toString(),
                    color = Color(0xFF00D9FF)
                )
                
                StatCard(
                    title = "Corrupted",
                    value = corruptedBits.toString(),
                    color = if (corruptedBits > 0) Color(0xFFFF3D71) else Color(0xFF4CAF50)
                )
                
                StatCard(
                    title = "QBER",
                    value = "${String.format("%.1f", qber * 100)}%",
                    color = when {
                        qber > 0.11 -> Color(0xFFFF3D71)
                        qber > 0.08 -> Color(0xFFFFA726)
                        else -> Color(0xFF4CAF50)
                    }
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // QBER Status
            if (detectedEve) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFFFF3D71).copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "🚨 EAVESDROPPING DETECTED! QBER > 11%",
                            color = Color(0xFFFF3D71),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            } else if (transmittedBits > 0) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = Color(0xFF4CAF50).copy(alpha = 0.2f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            "✅ Channel Secure - QBER below threshold",
                            color = Color(0xFF4CAF50),
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Controls
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1F3A)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Controls",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Start/Stop transmission
                    Button(
                        onClick = { isTransmitting = !isTransmitting },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isTransmitting) Color(0xFFFF3D71) else Color(0xFF00D9FF)
                        )
                    ) {
                        Icon(
                            if (isTransmitting) Icons.Default.Stop else Icons.Default.PlayArrow,
                            contentDescription = null
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(if (isTransmitting) "Stop Transmission" else "Start Transmission")
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Eve toggle
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "Enable Eve (Eavesdropper)",
                                color = Color.White,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                "Simulate man-in-the-middle attack",
                                color = Color(0xFF8E9AB3),
                                fontSize = 12.sp
                            )
                        }
                        Switch(
                            checked = eveEnabled,
                            onCheckedChange = { eveEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color(0xFFFF3D71),
                                checkedTrackColor = Color(0xFFFF3D71).copy(alpha = 0.5f)
                            )
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    // Reset button
                    OutlinedButton(
                        onClick = {
                            photons = emptyList()
                            transmittedBits = 0
                            corruptedBits = 0
                            qber = 0.0
                            detectedEve = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Reset Statistics")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Legend
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = Color(0xFF1A1F3A)
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp)
                ) {
                    Text(
                        "Legend",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LegendItem("🔵", "Blue photon", "+ basis (Rectilinear)")
                    LegendItem("🟣", "Purple photon", "× basis (Diagonal)")
                    LegendItem("👤", "Alice", "Sender (prepares photons)")
                    LegendItem("👁️", "Eve", "Eavesdropper (intercepts)")
                    LegendItem("👤", "Bob", "Receiver (measures photons)")
                    LegendItem("📊", "QBER", "Quantum Bit Error Rate")
                }
            }
        }
    }
}

@Composable
fun QuantumChannelCanvas(
    photons: List<Photon>,
    eveEnabled: Boolean,
    modifier: Modifier = Modifier
) {
    // Animation for pulsing effect
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )
    
    Canvas(modifier = modifier) {
        val canvasWidth = size.width
        val canvasHeight = size.height
        
        // Alice position (left)
        val aliceX = 60f
        val aliceY = canvasHeight / 2
        
        // Eve position (middle, if enabled)
        val eveX = canvasWidth / 2
        val eveY = canvasHeight / 2
        
        // Bob position (right)
        val bobX = canvasWidth - 60f
        val bobY = canvasHeight / 2
        
        // Draw quantum channel (line)
        drawLine(
            color = Color(0xFF2A3F5F),
            start = Offset(aliceX, aliceY),
            end = Offset(bobX, bobY),
            strokeWidth = 3f
        )
        
        // Draw Alice
        drawCircle(
            color = Color(0xFF00D9FF),
            radius = 30f,
            center = Offset(aliceX, aliceY),
            alpha = pulseAlpha
        )
        drawCircle(
            color = Color(0xFF00D9FF),
            radius = 25f,
            center = Offset(aliceX, aliceY)
        )
        
        // Draw Eve (if enabled)
        if (eveEnabled) {
            drawCircle(
                color = Color(0xFFFF3D71),
                radius = 30f,
                center = Offset(eveX, eveY),
                alpha = pulseAlpha * 0.5f
            )
            drawCircle(
                color = Color(0xFFFF3D71),
                radius = 25f,
                center = Offset(eveX, eveY)
            )
        }
        
        // Draw Bob
        drawCircle(
            color = Color(0xFF4CAF50),
            radius = 30f,
            center = Offset(bobX, bobY),
            alpha = pulseAlpha
        )
        drawCircle(
            color = Color(0xFF4CAF50),
            radius = 25f,
            center = Offset(bobX, bobY)
        )
        
        // Draw photons in transit
        photons.forEach { photon ->
            val photonX = aliceX + (bobX - aliceX) * photon.progress
            val photonY = aliceY
            
            // Photon color based on basis
            val photonColor = when (photon.aliceBasis) {
                Basis.RECTILINEAR -> Color(0xFF00D9FF) // Blue for +
                Basis.DIAGONAL -> Color(0xFFBB86FC)    // Purple for ×
            }
            
            // Draw photon
            drawCircle(
                color = photonColor,
                radius = 12f,
                center = Offset(photonX, photonY)
            )
            
            // Draw polarization indicator
            rotate(
                degrees = when (photon.aliceBasis) {
                    Basis.RECTILINEAR -> 0f  // Vertical/Horizontal
                    Basis.DIAGONAL -> 45f     // Diagonal
                },
                pivot = Offset(photonX, photonY)
            ) {
                drawLine(
                    color = Color.White,
                    start = Offset(photonX - 8f, photonY),
                    end = Offset(photonX + 8f, photonY),
                    strokeWidth = 3f
                )
            }
            
            // Draw interception indicator
            if (photon.interceptedByEve && photon.progress > 0.4f && photon.progress < 0.6f) {
                drawCircle(
                    color = Color(0xFFFF3D71),
                    radius = 20f,
                    center = Offset(photonX, photonY),
                    alpha = 0.3f
                )
            }
        }
    }
}

@Composable
fun StatCard(
    title: String,
    value: String,
    color: Color
) {
    Card(
        modifier = Modifier
            .width(110.dp)
            .height(90.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF1A1F3A)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                title,
                fontSize = 12.sp,
                color = Color(0xFF8E9AB3)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                value,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
fun LegendItem(
    icon: String,
    name: String,
    description: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            icon,
            fontSize = 20.sp,
            modifier = Modifier.width(40.dp)
        )
        Column {
            Text(
                name,
                color = Color.White,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                description,
                color = Color(0xFF8E9AB3),
                fontSize = 12.sp
            )
        }
    }
}

// Data models
data class Photon(
    val id: Int,
    val aliceBasis: Basis,
    val aliceBit: Boolean,
    val bobBasis: Basis,
    val progress: Float,
    val interceptedByEve: Boolean
)

enum class Basis {
    RECTILINEAR,  // + basis (0°/90°)
    DIAGONAL      // × basis (45°/135°)
}
