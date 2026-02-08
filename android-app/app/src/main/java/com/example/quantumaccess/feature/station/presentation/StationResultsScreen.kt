package com.example.quantumaccess.feature.station.presentation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.TrendingUp
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
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.graphics.nativeCanvas
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
import kotlin.random.Random

private data class HourlyStation(val hour: String, val count: Int)
private data class ActivityEntry(val time: String, val text: String, val type: String)

@Composable
fun StationResultsScreen(onBack: () -> Unit) {
    var totalVoters by remember { mutableIntStateOf(347) }
    var queueSize by remember { mutableIntStateOf(8) }

    val hourlyData = remember {
        listOf(
            HourlyStation("07:00", 12), HourlyStation("08:00", 28), HourlyStation("09:00", 41),
            HourlyStation("10:00", 38), HourlyStation("11:00", 45), HourlyStation("12:00", 52),
            HourlyStation("13:00", 36), HourlyStation("14:00", 48), HourlyStation("15:00", 47)
        )
    }

    var activityLog by remember {
        mutableStateOf(
            listOf(
                ActivityEntry("15:31", "Votant #347 — vot inregistrat cu succes", "success"),
                ActivityEntry("15:29", "Votant #346 — verificare identitate completa", "info"),
                ActivityEntry("15:27", "Votant #345 — cheie QKD generata (256 bit)", "quantum"),
                ActivityEntry("15:24", "Votant #344 — vot inregistrat cu succes", "success"),
                ActivityEntry("15:22", "Sistem — verificare integritate baza de date OK", "system"),
                ActivityEntry("15:19", "Votant #343 — vot inregistrat cu succes", "success"),
                ActivityEntry("15:15", "Sistem — conexiune quantum canal activ", "system"),
                ActivityEntry("15:12", "Votant #342 — vot inregistrat cu succes", "success")
            )
        )
    }

    // Simulate live updates
    LaunchedEffect(Unit) {
        while (true) {
            delay(6000)
            totalVoters++
            queueSize = (queueSize + Random.nextInt(-2, 3)).coerceIn(2, 15)
            activityLog = listOf(
                ActivityEntry(
                    "${15}:${(32 + activityLog.size).coerceAtMost(59)}",
                    "Votant #$totalVoters — vot inregistrat cu succes",
                    "success"
                )
            ) + activityLog.take(9)
        }
    }

    val stationRegistered = 820
    val turnout = totalVoters.toFloat() / stationRegistered * 100f
    val animatedTurnout by animateFloatAsState(turnout, tween(600, easing = FastOutSlowInEasing), label = "t")
    val nationalAvg = 42.3f

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
                            Text("Rezultate sectie", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Sectia nr. 43 · Liceul Grigore Moisil, Timisoara", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                        }
                        Icon(Icons.Filled.Refresh, null, tint = Emerald, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(4.dp))
                        Text("LIVE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Emerald)
                        Spacer(Modifier.width(12.dp))
                    }
                    Spacer(Modifier.height(16.dp))

                    // Big number
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("Prezenta la aceasta sectie", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                        Spacer(Modifier.height(4.dp))
                        Text(String.format("%.1f%%", animatedTurnout), fontSize = 48.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-1).sp)
                        Text("$totalVoters din $stationRegistered alegatori inscrisi", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                    }
                }
            }

            Column(
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                // Stats row
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MiniStat(Modifier.weight(1f), Icons.Filled.HowToVote, "$totalVoters", "Au votat", Emerald)
                    MiniStat(Modifier.weight(1f), Icons.Filled.Groups, "$queueSize", "In coada", Color(0xFFF59E0B))
                    MiniStat(Modifier.weight(1f), Icons.Filled.AccessTime, "~${queueSize * 2} min", "Asteptare", DeepBlue)
                }

                Spacer(Modifier.height(16.dp))

                // Comparison card
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardDefaults.cardColors(Color.White), CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Comparatie cu media nationala", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        Spacer(Modifier.height(12.dp))
                        ComparisonBar("Sectia #127", turnout, Emerald)
                        Spacer(Modifier.height(8.dp))
                        ComparisonBar("Media nationala", nationalAvg, DeepBlue)
                        Spacer(Modifier.height(8.dp))
                        val diff = turnout - nationalAvg
                        Text(
                            if (diff > 0) "Sectia este cu +${String.format("%.1f", diff)}% peste media nationala"
                            else "Sectia este cu ${String.format("%.1f", diff)}% sub media nationala",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (diff > 0) Emerald else Color(0xFFF59E0B),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Hourly breakdown
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardDefaults.cardColors(Color.White), CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.TrendingUp, null, tint = DeepBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Votanti pe ora", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        }
                        Spacer(Modifier.height(12.dp))
                        HourlyBarChart(hourlyData, Modifier.fillMaxWidth().height(140.dp))
                    }
                }

                Spacer(Modifier.height(16.dp))

                // Activity log
                Card(Modifier.fillMaxWidth(), RoundedCornerShape(16.dp), CardDefaults.cardColors(Color.White), CardDefaults.cardElevation(2.dp)) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.LocationOn, null, tint = DeepBlue, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Activitate recenta", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        }
                        Spacer(Modifier.height(12.dp))
                        activityLog.forEach { entry ->
                            ActivityRow(entry)
                        }
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun MiniStat(modifier: Modifier, icon: androidx.compose.ui.graphics.vector.ImageVector, value: String, label: String, color: Color) {
    Card(modifier, RoundedCornerShape(14.dp), CardDefaults.cardColors(Color.White), CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(icon, null, tint = color, modifier = Modifier.size(22.dp))
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = NightBlack)
            Text(label, style = MaterialTheme.typography.labelSmall, color = Slate800, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ComparisonBar(label: String, percent: Float, color: Color) {
    Column {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(label, style = MaterialTheme.typography.labelSmall, color = NightBlack, fontWeight = FontWeight.Medium)
            Text(String.format("%.1f%%", percent), style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = Color(0xFFE5E7EB)
        )
    }
}

@Composable
private fun HourlyBarChart(data: List<HourlyStation>, modifier: Modifier) {
    val maxVal = data.maxOfOrNull { it.count } ?: 1
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val padB = 28f
        val barW = (w / data.size) * 0.6f
        val gap = (w / data.size)
        val chartH = h - padB

        val lp = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
            color = android.graphics.Color.parseColor("#6B7280"); textSize = 20f
        }
        val vp = android.graphics.Paint().apply {
            textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true
            color = android.graphics.Color.parseColor("#1F2937"); textSize = 18f
            typeface = android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD)
        }

        data.forEachIndexed { i, d ->
            val barH = (d.count.toFloat() / maxVal) * (chartH - 20f)
            val x = i * gap + gap / 2 - barW / 2
            val y = chartH - barH

            // Bar gradient
            drawRect(
                Brush.verticalGradient(listOf(Color(0xFF6366F1), Color(0xFF818CF8)), y, chartH),
                Offset(x, y), Size(barW, barH)
            )
            // Rounded top
            drawCircle(Color(0xFF6366F1), barW / 2, Offset(x + barW / 2, y))

            // Value label
            drawContext.canvas.nativeCanvas.drawText("${d.count}", x + barW / 2, y - 6f, vp)
            // Hour label
            drawContext.canvas.nativeCanvas.drawText(d.hour.take(5), i * gap + gap / 2, h - 4f, lp)
        }
    }
}

@Composable
private fun ActivityRow(entry: ActivityEntry) {
    val color = when (entry.type) {
        "success" -> Emerald
        "quantum" -> Color(0xFF6366F1)
        "system" -> DeepBlue
        else -> Slate800
    }
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(entry.time, style = MaterialTheme.typography.labelSmall, color = Steel300, fontWeight = FontWeight.Medium)
        Spacer(Modifier.width(10.dp))
        Box(Modifier.padding(top = 4.dp).size(6.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(8.dp))
        Text(entry.text, style = MaterialTheme.typography.bodySmall, color = NightBlack, modifier = Modifier.weight(1f))
    }
}
