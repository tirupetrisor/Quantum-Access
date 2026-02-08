package com.example.quantumaccess.feature.results.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Emerald
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel300
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Calendar
import java.util.Locale

// ─── Data ──────────────────────────────────────────────────────────────────────

private data class CandidateResult(
    val name: String,
    val party: String,
    val votes: Int,
    val color: Color,
    val partyShort: String
)

private val sampleResults = listOf(
    CandidateResult("Elena Dragomir", "Alianta Viitorului", 3_245_820, Color(0xFFF59E0B), "AV"),
    CandidateResult("Andrei Petrescu", "Partidul Progresului Civic", 2_876_410, Color(0xFFEF4444), "PPC"),
    CandidateResult("Maria Enescu", "Miscarea pentru Echitate", 1_950_330, Color(0xFF6366F1), "MPE"),
    CandidateResult("Cristian Albescu", "Uniunea Cetateanului Liber", 1_432_180, Color(0xFF3B82F6), "UCL"),
    CandidateResult("Ana Moldovan", "Frontul Noii Generatii", 987_650, Color(0xFF059669), "FNG"),
    CandidateResult("Bogdan Olariu", "Partidul Verde Digital", 543_210, Color(0xFF8B5CF6), "PVD"),
    CandidateResult("Alte partide", "Partide sub prag", 876_400, Color(0xFF94A3B8), "ALTE")
)

private val nf = NumberFormat.getNumberInstance(Locale("ro", "RO"))

// ─── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun ElectionResultsScreen(onBack: () -> Unit) {
    val currentHour = remember { Calendar.getInstance().get(Calendar.HOUR_OF_DAY) }
    // For demo: set to true to show results regardless of hour
    // In production: val isAvailable = currentHour >= 21
    var isDemoMode by remember { mutableStateOf(false) }
    val isAvailable = currentHour >= 21 || isDemoMode

    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { delay(100); visible = true }

    val totalVotes = sampleResults.sumOf { it.votes }
    val totalRegistered = 18_200_000

    Box(
        Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
            .navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Box(
                Modifier
                    .fillMaxWidth()
                    .background(Brush.verticalGradient(listOf(DeepBlue, Color(0xFF0D1B4A))))
                    .statusBarsPadding()
                    .padding(bottom = 20.dp)
            ) {
                Column {
                    Row(
                        Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Filled.ArrowBack, null, tint = Color.White)
                        }
                        Spacer(Modifier.width(4.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Rezultate alegeri", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Alegeri Parlamentare 2026", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                        }
                        if (isAvailable) {
                            Icon(Icons.Filled.Verified, null, tint = Emerald, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("OFICIAL", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Emerald)
                        } else {
                            Icon(Icons.Filled.Lock, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("BLOCAT", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Color(0xFFF59E0B))
                        }
                        Spacer(Modifier.width(12.dp))
                    }
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                if (!isAvailable) {
                    // Locked state
                    Spacer(Modifier.height(40.dp))
                    AnimatedVisibility(visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { 40 }) {
                        Card(
                            Modifier.fillMaxWidth(),
                            RoundedCornerShape(20.dp),
                            CardDefaults.cardColors(Color.White),
                            CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(
                                Modifier.padding(32.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    Modifier.size(80.dp).clip(CircleShape).background(Color(0xFFFEF3C7)),
                                    Alignment.Center
                                ) {
                                    Icon(Icons.Filled.Lock, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(40.dp))
                                }
                                Spacer(Modifier.height(20.dp))
                                Text("Rezultatele nu sunt inca disponibile", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NightBlack, textAlign = TextAlign.Center)
                                Spacer(Modifier.height(8.dp))
                                Text(
                                    "Rezultatele vor fi afisate dupa ora 21:00, cand se inchid sectiile de votare din toata tara.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Slate800,
                                    textAlign = TextAlign.Center
                                )
                                Spacer(Modifier.height(20.dp))

                                // Countdown info
                                Card(
                                    Modifier.fillMaxWidth(),
                                    RoundedCornerShape(12.dp),
                                    CardDefaults.cardColors(DeepBlue.copy(alpha = 0.06f))
                                ) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Schedule, null, tint = DeepBlue, modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Sectiile se inchid la ora 21:00", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NightBlack)
                                            val hoursLeft = (21 - currentHour).coerceAtLeast(0)
                                            Text("Mai sunt ~$hoursLeft ore pana la afisarea rezultatelor", style = MaterialTheme.typography.bodySmall, color = Slate800)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Security info
                                Card(
                                    Modifier.fillMaxWidth(),
                                    RoundedCornerShape(12.dp),
                                    CardDefaults.cardColors(Emerald.copy(alpha = 0.06f))
                                ) {
                                    Row(
                                        Modifier.padding(16.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Icon(Icons.Filled.Verified, null, tint = Emerald, modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(12.dp))
                                        Column {
                                            Text("Voturile sunt securizate cu QKD", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NightBlack)
                                            Text("Fiecare vot este criptat prin protocolul BB84 si verificat anti-tamper", style = MaterialTheme.typography.bodySmall, color = Slate800)
                                        }
                                    }
                                }

                                Spacer(Modifier.height(16.dp))

                                // Demo button
                                Text(
                                    "Demo: apasa pentru a vedea rezultatele simulate",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = Steel300,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFFF1F5F9))
                                        .padding(horizontal = 16.dp, vertical = 8.dp)
                                        .then(
                                            Modifier.run {
                                                this
                                            }
                                        ),
                                )
                                // Clickable demo toggle
                                Spacer(Modifier.height(4.dp))
                                androidx.compose.material3.TextButton(onClick = { isDemoMode = true }) {
                                    Text("Activeaza modul demo", color = DeepBlue, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                } else {
                    // Results available
                    AnimatedVisibility(visible, enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 30 }) {
                        Column {
                            // Summary card
                            Card(
                                Modifier.fillMaxWidth(),
                                RoundedCornerShape(16.dp),
                                CardDefaults.cardColors(Color.White),
                                CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(20.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.EmojiEvents, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(24.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Rezultate finale", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NightBlack)
                                    }
                                    Spacer(Modifier.height(4.dp))
                                    Text("Numaratoare completa · 100% sectii raportate", style = MaterialTheme.typography.labelSmall, color = Emerald, fontWeight = FontWeight.SemiBold)

                                    Spacer(Modifier.height(16.dp))

                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                                        MiniStatBox(Icons.Filled.HowToVote, nf.format(totalVotes), "Voturi valide", DeepBlue)
                                        MiniStatBox(Icons.Filled.Groups, nf.format(totalRegistered), "Alegatori inscrisi", Color(0xFF6366F1))
                                        MiniStatBox(Icons.Filled.Verified, String.format("%.1f%%", totalVotes.toFloat() / totalRegistered * 100), "Prezenta finala", Emerald)
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Donut chart
                            Card(
                                Modifier.fillMaxWidth(),
                                RoundedCornerShape(16.dp),
                                CardDefaults.cardColors(Color.White),
                                CardDefaults.cardElevation(2.dp)
                            ) {
                                Column(Modifier.padding(20.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Distributia voturilor", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                                    Spacer(Modifier.height(16.dp))
                                    DonutChart(
                                        results = sampleResults,
                                        totalVotes = totalVotes,
                                        modifier = Modifier.size(200.dp)
                                    )
                                    Spacer(Modifier.height(12.dp))
                                    // Legend
                                    sampleResults.take(5).forEach { result ->
                                        Row(
                                            Modifier.fillMaxWidth().padding(vertical = 2.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Box(Modifier.size(10.dp).clip(CircleShape).background(result.color))
                                            Spacer(Modifier.width(8.dp))
                                            Text(result.partyShort, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NightBlack, modifier = Modifier.width(36.dp))
                                            Text(result.party, style = MaterialTheme.typography.bodySmall, color = Slate800, modifier = Modifier.weight(1f))
                                            Text(String.format("%.1f%%", result.votes.toFloat() / totalVotes * 100), style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = result.color)
                                        }
                                    }
                                }
                            }

                            Spacer(Modifier.height(16.dp))

                            // Candidate results
                            Text("Clasament candidati / partide", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                            Spacer(Modifier.height(8.dp))

                            sampleResults.forEachIndexed { index, result ->
                                CandidateCard(index + 1, result, totalVotes)
                                Spacer(Modifier.height(8.dp))
                            }

                            Spacer(Modifier.height(12.dp))

                            // QKD verification
                            Card(
                                Modifier.fillMaxWidth(),
                                RoundedCornerShape(12.dp),
                                CardDefaults.cardColors(Emerald.copy(alpha = 0.06f))
                            ) {
                                Column(Modifier.padding(16.dp)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(Icons.Filled.Verified, null, tint = Emerald, modifier = Modifier.size(20.dp))
                                        Spacer(Modifier.width(8.dp))
                                        Text("Verificare QKD completa", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    VerifyRow("Voturi verificate cu QKD", "${nf.format(totalVotes)} / ${nf.format(totalVotes)}")
                                    VerifyRow("QBER mediu", "2.3% (sub prag 11%)")
                                    VerifyRow("Incercari de interceptare Eve", "0 detectate")
                                    VerifyRow("Integritate blockchain", "100% — hash valid")
                                    VerifyRow("Certificat BB84", "Valid · Emis de AEP")
                                }
                            }

                            Spacer(Modifier.height(24.dp))
                        }
                    }
                }
            }
        }
    }
}

// ─── Components ────────────────────────────────────────────────────────────────

@Composable
private fun MiniStatBox(icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            Modifier.size(36.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)),
            Alignment.Center
        ) {
            Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(6.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NightBlack, textAlign = TextAlign.Center)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Slate800, fontSize = 9.sp, textAlign = TextAlign.Center)
    }
}

@Composable
private fun DonutChart(results: List<CandidateResult>, totalVotes: Int, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val strokeWidth = 40f
        val radius = (size.minDimension - strokeWidth) / 2
        val center = Offset(size.width / 2, size.height / 2)

        var startAngle = -90f
        results.forEach { result ->
            val sweepAngle = (result.votes.toFloat() / totalVotes) * 360f
            drawArc(
                color = result.color,
                startAngle = startAngle,
                sweepAngle = sweepAngle - 1.5f,
                useCenter = false,
                topLeft = Offset(center.x - radius, center.y - radius),
                size = Size(radius * 2, radius * 2),
                style = Stroke(width = strokeWidth, cap = StrokeCap.Butt)
            )
            startAngle += sweepAngle
        }

        // Inner circle for donut effect (white center)
        drawCircle(
            color = Color.White,
            radius = radius - strokeWidth / 2 - 4f,
            center = center
        )
    }
}

@Composable
private fun CandidateCard(rank: Int, result: CandidateResult, totalVotes: Int) {
    val percent = result.votes.toFloat() / totalVotes * 100f
    val animatedProgress by animateFloatAsState(percent / 100f, tween(600), label = "cp")

    Card(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(14.dp),
        CardDefaults.cardColors(Color.White),
        CardDefaults.cardElevation(1.dp)
    ) {
        Column(Modifier.padding(14.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier.size(32.dp).clip(CircleShape).background(
                        when (rank) { 1 -> Color(0xFFFBBF24); 2 -> Color(0xFF94A3B8); 3 -> Color(0xFFCD7F32); else -> Color(0xFFE5E7EB) }
                    ),
                    Alignment.Center
                ) {
                    Text("$rank", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (rank <= 3) Color.White else NightBlack)
                }
                Spacer(Modifier.width(10.dp))
                Column(Modifier.weight(1f)) {
                    Text(result.name, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.clip(RoundedCornerShape(3.dp)).background(result.color.copy(alpha = 0.15f)).padding(horizontal = 6.dp, vertical = 1.dp)
                        ) {
                            Text(result.partyShort, fontSize = 9.sp, fontWeight = FontWeight.Bold, color = result.color)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(result.party, style = MaterialTheme.typography.labelSmall, color = Slate800)
                    }
                }
                Text(String.format("%.1f%%", percent), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = result.color)
            }
            Spacer(Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = result.color,
                trackColor = Color(0xFFE5E7EB)
            )
            Spacer(Modifier.height(4.dp))
            Text("${nf.format(result.votes)} voturi", style = MaterialTheme.typography.labelSmall, color = Steel300)
        }
    }
}

@Composable
private fun VerifyRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text("  ✓  ", color = Emerald, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(label, style = MaterialTheme.typography.bodySmall, color = Slate800, modifier = Modifier.weight(1f))
        Text(value, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.SemiBold, color = NightBlack)
    }
}
