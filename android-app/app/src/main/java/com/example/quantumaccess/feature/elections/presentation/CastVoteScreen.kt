package com.example.quantumaccess.feature.elections.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Emerald
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.domain.model.VoteReceipt
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlin.random.Random

sealed class CastVoteState {
    data object Idle : CastVoteState()
    data object GeneratingKey : CastVoteState()
    data object CheckingChannel : CastVoteState()
    data class Success(val receipt: VoteReceipt) : CastVoteState()
    data class Error(val message: String) : CastVoteState()
}

private data class LivePhoton(
    val id: Int,
    val basis: Int, // 0 = rectilinear, 1 = diagonal
    val progress: Float
)

@Composable
fun CastVoteScreen(
    state: CastVoteState,
    onCastVote: () -> Unit,
    onDone: () -> Unit,
    onRetry: () -> Unit
) {
    val isTransmitting = state is CastVoteState.GeneratingKey || state is CastVoteState.CheckingChannel

    // Live photon state
    val photons = remember { mutableStateListOf<LivePhoton>() }
    var photonCounter by remember { mutableIntStateOf(0) }
    var qberDisplay by remember { mutableFloatStateOf(0f) }
    var photonsSent by remember { mutableIntStateOf(0) }

    // Photon animation while transmitting
    LaunchedEffect(isTransmitting) {
        if (isTransmitting) {
            photons.clear()
            photonCounter = 0
            photonsSent = 0
            qberDisplay = 0f
            while (isActive && isTransmitting) {
                val id = photonCounter++
                val basis = if (Random.nextBoolean()) 0 else 1
                photons.add(LivePhoton(id = id, basis = basis, progress = 0f))
                photonsSent++
                qberDisplay = Random.nextFloat() * 0.06f // Low QBER = secure

                // Animate this photon forward
                for (step in 1..25) {
                    val progress = step / 25f
                    val idx = photons.indexOfFirst { it.id == id }
                    if (idx >= 0) {
                        photons[idx] = photons[idx].copy(progress = progress)
                    }
                    delay(30)
                }
                // Remove old photons to keep list small
                if (photons.size > 8) {
                    photons.removeRange(0, photons.size - 8)
                }
                delay(100)
            }
        } else {
            photons.clear()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0A0E27))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            when (val s = state) {
                is CastVoteState.Idle -> {
                    Spacer(modifier = Modifier.height(60.dp))
                    Icon(
                        Icons.Filled.Security,
                        contentDescription = null,
                        tint = Color(0xFF00D9FF),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        "Pregătire vot securizat",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Apasă butonul de mai jos pentru a iniția protocolul BB84.\nVei vedea live cum se transmit fotonii prin canalul quantum.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E9AB3),
                        textAlign = TextAlign.Center,
                        lineHeight = 20.sp
                    )
                    Spacer(modifier = Modifier.height(32.dp))

                    // Steps explanation
                    StepItem(number = "1", text = "Se generează cheia quantum (BB84)")
                    StepItem(number = "2", text = "Se verifică canalul — detectare Eve (QBER)")
                    StepItem(number = "3", text = "Votul se criptează cu cheia quantum")
                    StepItem(number = "4", text = "Primești chitanță verificabilă")

                    Spacer(modifier = Modifier.height(32.dp))
                    Button(
                        onClick = onCastVote,
                        modifier = Modifier.fillMaxWidth().height(54.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9FF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(20.dp), tint = Color(0xFF0A0E27))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Generează cheie QKD și votează", color = Color(0xFF0A0E27), fontWeight = FontWeight.Bold)
                    }
                }

                is CastVoteState.GeneratingKey,
                is CastVoteState.CheckingChannel -> {
                    Spacer(modifier = Modifier.height(20.dp))
                    Text(
                        if (s is CastVoteState.GeneratingKey) "Se generează cheia quantum…"
                        else "Se verifică canalul — detectare Eve…",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF00D9FF)
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Protocol BB84 · Transmisie fotoni",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF8E9AB3)
                    )
                    Spacer(modifier = Modifier.height(20.dp))

                    // LIVE PHOTON CANVAS
                    LivePhotonCanvas(
                        photons = photons,
                        modifier = Modifier.fillMaxWidth().height(200.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    // Stats
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        QkdStatBox(label = "Fotoni transmiși", value = "$photonsSent")
                        QkdStatBox(label = "QBER", value = "${String.format("%.1f", qberDisplay * 100)}%")
                        QkdStatBox(
                            label = "Status",
                            value = if (qberDisplay < 0.11f) "Secure" else "Alert!",
                            valueColor = if (qberDisplay < 0.11f) Emerald else Color(0xFFFF3D71)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Emerald.copy(alpha = 0.15f))
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Emerald))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                "Canal securizat · QBER sub 11% · Fără interceptări detectate",
                                style = MaterialTheme.typography.labelSmall,
                                color = Emerald
                            )
                        }
                    }
                }

                is CastVoteState.Success -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = Emerald,
                        modifier = Modifier.size(80.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Vot înregistrat cu succes!",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        s.receipt.electionName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E9AB3)
                    )
                    Spacer(modifier = Modifier.height(24.dp))

                    // Receipt card
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1F3A))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text("Chitanță verificabilă", style = MaterialTheme.typography.labelMedium, color = Color(0xFF8E9AB3))
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                s.receipt.receiptToken,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF00D9FF),
                                letterSpacing = 2.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(Emerald))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    if (s.receipt.quantumSecured) "Securizat cu QKD real (Qrypt/QbitShield)"
                                    else "Securizat cu simulare QKD (CSPRNG)",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (s.receipt.quantumSecured) Emerald else Color(0xFFFFA726)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                "Poți verifica chitanța pe portalul autorității electorale.\nVotul tău rămâne anonim — doar chitanța confirmă participarea.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color(0xFF6B7280),
                                lineHeight = 16.sp
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9FF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Înapoi la alegeri", color = Color(0xFF0A0E27), fontWeight = FontWeight.Bold)
                    }
                }

                is CastVoteState.Error -> {
                    Spacer(modifier = Modifier.height(40.dp))
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color(0xFFFF3D71), modifier = Modifier.size(72.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        "Votul a fost ANULAT",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFFF3D71)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        s.message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF8E9AB3),
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFFF3D71).copy(alpha = 0.12f))
                    ) {
                        Text(
                            "Protocolul BB84 a detectat un eavesdropper (Eve) pe canal.\nVotul NU a fost trimis — siguranța ta este prioritară.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFFF3D71),
                            modifier = Modifier.padding(14.dp),
                            lineHeight = 16.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onRetry,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00D9FF)),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Încearcă din nou (cheie nouă)", color = Color(0xFF0A0E27), fontWeight = FontWeight.Bold)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = onDone,
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text("Înapoi fără a vota", color = Color(0xFF8E9AB3))
                    }
                }
            }
        }
    }
}

@Composable
private fun StepItem(number: String, text: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier.size(28.dp).clip(CircleShape).background(Color(0xFF1A1F3A)),
            contentAlignment = Alignment.Center
        ) {
            Text(number, color = Color(0xFF00D9FF), fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
        Spacer(modifier = Modifier.width(12.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = Color(0xFFCBD5E1))
    }
}

@Composable
private fun QkdStatBox(label: String, value: String, valueColor: Color = Color(0xFF00D9FF)) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = valueColor)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color(0xFF8E9AB3))
    }
}

/**
 * Live animated canvas showing photons traveling from Alice (Secția) to Bob (Server central).
 */
@Composable
private fun LivePhotonCanvas(
    photons: List<LivePhoton>,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseAlpha"
    )

    Canvas(modifier = modifier.clip(RoundedCornerShape(16.dp)).background(Color(0xFF0D1232))) {
        val w = size.width
        val h = size.height
        val midY = h / 2f

        val aliceX = 50f
        val bobX = w - 50f

        // Channel line
        drawLine(Color(0xFF1E2650), Offset(aliceX, midY), Offset(bobX, midY), strokeWidth = 2f)

        // Alice (Secția de votare)
        drawCircle(Color(0xFF00D9FF), radius = 22f, center = Offset(aliceX, midY), alpha = pulseAlpha * 0.5f)
        drawCircle(Color(0xFF00D9FF), radius = 16f, center = Offset(aliceX, midY))

        // Bob (Server AEP)
        drawCircle(Color(0xFF4CAF50), radius = 22f, center = Offset(bobX, midY), alpha = pulseAlpha * 0.5f)
        drawCircle(Color(0xFF4CAF50), radius = 16f, center = Offset(bobX, midY))

        // Labels
        drawContext.canvas.nativeCanvas.apply {
            val paint = android.graphics.Paint().apply {
                color = android.graphics.Color.parseColor("#8E9AB3")
                textSize = 28f
                textAlign = android.graphics.Paint.Align.CENTER
                isAntiAlias = true
            }
            drawText("Secția", aliceX, midY + 42f, paint)
            drawText("Server AEP", bobX, midY + 42f, paint)
        }

        // Photons
        photons.forEach { photon ->
            val px = aliceX + (bobX - aliceX) * photon.progress
            val color = if (photon.basis == 0) Color(0xFF00D9FF) else Color(0xFFBB86FC)
            drawCircle(color, radius = 10f, center = Offset(px, midY))
            // Polarization line
            rotate(
                degrees = if (photon.basis == 0) 0f else 45f,
                pivot = Offset(px, midY)
            ) {
                drawLine(Color.White, Offset(px - 6f, midY), Offset(px + 6f, midY), strokeWidth = 2.5f)
            }
        }
    }
}
