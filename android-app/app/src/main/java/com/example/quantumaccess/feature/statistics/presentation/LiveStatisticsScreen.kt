package com.example.quantumaccess.feature.statistics.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Public
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
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.R
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Emerald
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel300
import kotlinx.coroutines.delay
import java.text.NumberFormat
import java.util.Locale
import kotlin.random.Random

// ─── Data Models ───────────────────────────────────────────────────────────────

private data class CountyTurnout(
    val code: String,
    val name: String,
    val registeredVoters: Int,
    val votesCast: Int
) {
    val turnoutPercent: Float
        get() = if (registeredVoters > 0) votesCast.toFloat() / registeredVoters * 100f else 0f
}

private data class HourlyPoint(val hour: Int, val turnout: Float)

// ─── County Data ──────────────────────────────────────────────────────────────

private data class CountyInfo(val code: String, val name: String, val registered: Int)

private val allCounties = listOf(
    CountyInfo("AB", "Alba", 305000),
    CountyInfo("AR", "Arad", 385000),
    CountyInfo("AG", "Arges", 525000),
    CountyInfo("BC", "Bacau", 550000),
    CountyInfo("BH", "Bihor", 495000),
    CountyInfo("BN", "Bistrita-N.", 255000),
    CountyInfo("BT", "Botosani", 355000),
    CountyInfo("BV", "Brasov", 510000),
    CountyInfo("BR", "Braila", 280000),
    CountyInfo("B", "Bucuresti", 1850000),
    CountyInfo("BZ", "Buzau", 385000),
    CountyInfo("CS", "Caras-Severin", 255000),
    CountyInfo("CL", "Calarasi", 245000),
    CountyInfo("CJ", "Cluj", 650000),
    CountyInfo("CT", "Constanta", 610000),
    CountyInfo("CV", "Covasna", 175000),
    CountyInfo("DB", "Dambovita", 425000),
    CountyInfo("DJ", "Dolj", 555000),
    CountyInfo("GL", "Galati", 480000),
    CountyInfo("GR", "Giurgiu", 225000),
    CountyInfo("GJ", "Gorj", 295000),
    CountyInfo("HR", "Harghita", 258000),
    CountyInfo("HD", "Hunedoara", 365000),
    CountyInfo("IL", "Ialomita", 230000),
    CountyInfo("IS", "Iasi", 680000),
    CountyInfo("IF", "Ilfov", 395000),
    CountyInfo("MM", "Maramures", 410000),
    CountyInfo("MH", "Mehedinti", 230000),
    CountyInfo("MS", "Mures", 470000),
    CountyInfo("NT", "Neamt", 380000),
    CountyInfo("OT", "Olt", 355000),
    CountyInfo("PH", "Prahova", 665000),
    CountyInfo("SM", "Satu Mare", 310000),
    CountyInfo("SJ", "Salaj", 195000),
    CountyInfo("SB", "Sibiu", 365000),
    CountyInfo("SV", "Suceava", 560000),
    CountyInfo("TR", "Teleorman", 315000),
    CountyInfo("TM", "Timis", 590000),
    CountyInfo("TL", "Tulcea", 195000),
    CountyInfo("VS", "Vaslui", 365000),
    CountyInfo("VL", "Valcea", 320000),
    CountyInfo("VN", "Vrancea", 305000)
)

// ─── Sample Data ───────────────────────────────────────────────────────────────

private fun generateCountyData(): List<CountyTurnout> {
    return allCounties.map { c ->
        val turnout = Random.nextFloat() * 0.25f + 0.28f
        CountyTurnout(
            code = c.code,
            name = c.name,
            registeredVoters = c.registered,
            votesCast = (c.registered * turnout).toInt()
        )
    }
}

private fun generateHourlyData(): List<HourlyPoint> = listOf(
    HourlyPoint(7, 0.0f), HourlyPoint(8, 5.2f), HourlyPoint(9, 12.4f),
    HourlyPoint(10, 19.1f), HourlyPoint(11, 25.7f), HourlyPoint(12, 31.3f),
    HourlyPoint(13, 36.8f), HourlyPoint(14, 41.2f), HourlyPoint(15, 44.6f)
)

// ─── Color Helpers ─────────────────────────────────────────────────────────────

private fun turnoutColor(percent: Float): Color = when {
    percent < 20f -> Color(0xFFEF4444)
    percent < 35f -> Color(0xFFF97316)
    percent < 45f -> Color(0xFFEAB308)
    percent < 55f -> Color(0xFF22C55E)
    else -> Color(0xFF059669)
}

private val nf = NumberFormat.getNumberInstance(Locale("ro", "RO"))

// ─── Main Screen ───────────────────────────────────────────────────────────────

@Composable
fun LiveStatisticsScreen(onBack: () -> Unit) {
    var counties by remember { mutableStateOf(generateCountyData()) }
    val hourlyData by remember { mutableStateOf(generateHourlyData()) }
    var lastUpdate by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var visible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) { delay(100); visible = true }

    // Simulated live updates
    LaunchedEffect(Unit) {
        while (true) {
            delay(4000)
            counties = counties.map { c ->
                val inc = Random.nextInt(0, (c.registeredVoters / 3000).coerceAtLeast(1))
                c.copy(votesCast = (c.votesCast + inc).coerceAtMost(c.registeredVoters))
            }
            lastUpdate = System.currentTimeMillis()
        }
    }

    val totalRegistered = counties.sumOf { it.registeredVoters }
    val totalVoted = counties.sumOf { it.votesCast }
    val nationalTurnout = if (totalRegistered > 0) totalVoted.toFloat() / totalRegistered * 100f else 0f
    val animatedTurnout by animateFloatAsState(
        targetValue = nationalTurnout,
        animationSpec = tween(800, easing = FastOutSlowInEasing), label = "t"
    )
    val sortedByTurnout = remember(counties) { counties.sortedByDescending { it.turnoutPercent } }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
            .navigationBarsPadding()
    ) {
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            // ── Header ─────────────────
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Brush.verticalGradient(listOf(DeepBlue, Color(0xFF0D1B4A))))
                        .statusBarsPadding()
                        .padding(bottom = 24.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                            }
                            Spacer(Modifier.width(4.dp))
                            Column(Modifier.weight(1f)) {
                                Text("Statistici Live", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                                Text("Alegeri Parlamentare 2026", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                            }
                            Icon(Icons.Filled.Refresh, contentDescription = null, tint = Emerald, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(4.dp))
                            Text("LIVE", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = Emerald)
                            Spacer(Modifier.width(12.dp))
                        }
                        Spacer(Modifier.height(12.dp))
                        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            TimeChip(Icons.Filled.AccessTime, "Ora curenta", "15:32")
                            TimeChip(Icons.Filled.HowToVote, "Sectii deschise", "18.782")
                            TimeChip(Icons.Filled.Groups, "Program", "07-21")
                        }
                        Spacer(Modifier.height(20.dp))
                        AnimatedVisibility(visible, enter = fadeIn(tween(600)) + slideInVertically(tween(600)) { -40 }) {
                            Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                                Text("Prezenta nationala", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.7f))
                                Spacer(Modifier.height(4.dp))
                                Text(String.format("%.1f%%", animatedTurnout), fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White, letterSpacing = (-1).sp)
                                Spacer(Modifier.height(4.dp))
                                Text("${nf.format(totalVoted)} din ${nf.format(totalRegistered)} alegatori inscrisi", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.6f))
                            }
                        }
                    }
                }
            }

            // ── Stat Cards ─────────────
            item {
                Spacer(Modifier.height(16.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    StatCard(Modifier.weight(1f), "Au votat", nf.format(totalVoted), null, Icons.Filled.HowToVote, Emerald)
                    StatCard(Modifier.weight(1f), "Cel mai activ", sortedByTurnout.firstOrNull()?.code ?: "-", String.format("%.1f%%", sortedByTurnout.firstOrNull()?.turnoutPercent ?: 0f), Icons.Filled.TrendingUp, Color(0xFF6366F1))
                    StatCard(Modifier.weight(1f), "Judete", "42", null, Icons.Filled.Public, DeepBlue)
                }
            }

            // ── Romania Map (Real Image) ──
            item {
                Spacer(Modifier.height(20.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Public, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Harta prezentei pe judete", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        }
                        Spacer(Modifier.height(12.dp))

                        // Real Romania map image
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFFF1F5F9)),
                            contentAlignment = Alignment.Center
                        ) {
                            Image(
                                painter = painterResource(R.drawable.romania_map),
                                contentDescription = "Harta Romaniei pe judete",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                contentScale = ContentScale.FillWidth
                            )
                        }

                        Spacer(Modifier.height(12.dp))

                        // Turnout legend with colors
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                            LegendItem(Color(0xFFEF4444), "<20%")
                            LegendItem(Color(0xFFF97316), "20-35%")
                            LegendItem(Color(0xFFEAB308), "35-45%")
                            LegendItem(Color(0xFF22C55E), "45-55%")
                            LegendItem(Color(0xFF059669), ">55%")
                        }

                        Spacer(Modifier.height(12.dp))

                        // Top 5 quick glance
                        Text("Top 5 judete — prezenta la vot", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NightBlack)
                        Spacer(Modifier.height(8.dp))
                        sortedByTurnout.take(5).forEachIndexed { index, county ->
                            Row(
                                Modifier.fillMaxWidth().padding(vertical = 3.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                val medal = when (index) { 0 -> "🥇"; 1 -> "🥈"; 2 -> "🥉"; else -> "  ${index + 1}." }
                                Text(medal, fontSize = 14.sp, modifier = Modifier.width(28.dp))
                                Spacer(Modifier.width(4.dp))
                                Text(county.code, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DeepBlue, modifier = Modifier.width(28.dp))
                                Text(county.name, style = MaterialTheme.typography.bodySmall, color = NightBlack, modifier = Modifier.weight(1f))
                                LinearProgressIndicator(
                                    progress = { county.turnoutPercent / 100f },
                                    modifier = Modifier.width(60.dp).height(6.dp).clip(RoundedCornerShape(3.dp)),
                                    color = turnoutColor(county.turnoutPercent),
                                    trackColor = Color(0xFFE5E7EB)
                                )
                                Spacer(Modifier.width(8.dp))
                                Text(String.format("%.1f%%", county.turnoutPercent), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = turnoutColor(county.turnoutPercent), modifier = Modifier.width(42.dp), textAlign = TextAlign.End)
                            }
                        }
                    }
                }
            }

            // ── Hourly Chart ───────────
            item {
                Spacer(Modifier.height(16.dp))
                Card(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.BarChart, contentDescription = null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Evolutia prezentei (pe ore)", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        }
                        Spacer(Modifier.height(12.dp))
                        HourlyChart(hourlyData, Modifier.fillMaxWidth().height(160.dp))
                    }
                }
            }

            // ── County Ranking ─────────
            item {
                Spacer(Modifier.height(20.dp))
                Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text("Clasament judete", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                    Spacer(Modifier.weight(1f))
                    Text("${sortedByTurnout.size} judete", style = MaterialTheme.typography.labelSmall, color = Slate800)
                }
                Spacer(Modifier.height(8.dp))
            }
            itemsIndexed(sortedByTurnout) { index, county ->
                CountyRankCard(index + 1, county, Modifier.padding(horizontal = 16.dp, vertical = 3.dp))
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

// ─── Small Components ──────────────────────────────────────────────────────────

@Composable
private fun TimeChip(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color.White.copy(alpha = 0.6f), modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Color.White)
        Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f), fontSize = 9.sp)
    }
}

@Composable
private fun StatCard(modifier: Modifier, title: String, value: String, subtitle: String?, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Card(modifier, RoundedCornerShape(14.dp), CardDefaults.cardColors(Color.White), CardDefaults.cardElevation(2.dp)) {
        Column(Modifier.padding(12.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(Modifier.size(32.dp).clip(CircleShape).background(color.copy(alpha = 0.12f)), Alignment.Center) {
                Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.height(6.dp))
            Text(value, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack, maxLines = 1, overflow = TextOverflow.Ellipsis)
            if (subtitle != null) Text(subtitle, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.SemiBold)
            Text(title, style = MaterialTheme.typography.labelSmall, color = Slate800, fontSize = 9.sp)
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(10.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = MaterialTheme.typography.labelSmall, color = Slate800, fontSize = 9.sp)
    }
}

// ─── Hourly Chart ──────────────────────────────────────────────────────────────

@Composable
private fun HourlyChart(data: List<HourlyPoint>, modifier: Modifier) {
    Canvas(modifier = modifier) {
        val w = size.width; val h = size.height
        val padL = 40f; val padB = 30f; val padT = 10f; val padR = 10f
        val cW = w - padL - padR; val cH = h - padB - padT; val maxT = 60f

        val lp = android.graphics.Paint().apply { textAlign = android.graphics.Paint.Align.CENTER; isAntiAlias = true; color = android.graphics.Color.parseColor("#9CA3AF"); textSize = 22f }
        val ylp = android.graphics.Paint().apply { textAlign = android.graphics.Paint.Align.RIGHT; isAntiAlias = true; color = android.graphics.Color.parseColor("#9CA3AF"); textSize = 20f }

        for (pct in listOf(0f, 15f, 30f, 45f, 60f)) {
            val y = padT + cH * (1f - pct / maxT)
            drawLine(Color(0xFFE5E7EB), Offset(padL, y), Offset(padL + cW, y), 1f)
            drawContext.canvas.nativeCanvas.drawText("${pct.toInt()}%", padL - 6f, y + 6f, ylp)
        }
        if (data.size < 2) return@Canvas
        val pts = data.map { Offset(padL + ((it.hour - 7f) / 14f) * cW, padT + cH * (1f - it.turnout / maxT)) }

        val fill = Path().apply { moveTo(pts.first().x, padT + cH); pts.forEach { lineTo(it.x, it.y) }; lineTo(pts.last().x, padT + cH); close() }
        drawPath(fill, Brush.verticalGradient(listOf(Color(0xFF6366F1).copy(alpha = 0.3f), Color(0xFF6366F1).copy(alpha = 0.02f)), padT, padT + cH))

        val line = Path().apply { moveTo(pts.first().x, pts.first().y); for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y) }
        drawPath(line, Color(0xFF6366F1), style = Stroke(3f, cap = StrokeCap.Round))
        pts.forEach { drawCircle(Color.White, 5f, it); drawCircle(Color(0xFF6366F1), 4f, it) }
        data.forEach { dp -> drawContext.canvas.nativeCanvas.drawText("${dp.hour}:00", padL + ((dp.hour - 7f) / 14f) * cW, h - 4f, lp.apply { textSize = 18f }) }
    }
}

// ─── County Rank Card ──────────────────────────────────────────────────────────

@Composable
private fun CountyRankCard(rank: Int, county: CountyTurnout, modifier: Modifier) {
    val color = turnoutColor(county.turnoutPercent)
    val anim by animateFloatAsState(county.turnoutPercent / 100f, tween(600), label = "p")

    Card(modifier.fillMaxWidth(), RoundedCornerShape(12.dp), CardDefaults.cardColors(Color.White), CardDefaults.cardElevation(1.dp)) {
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(30.dp).clip(CircleShape).background(when (rank) { 1 -> Color(0xFFFBBF24); 2 -> Color(0xFF94A3B8); 3 -> Color(0xFFCD7F32); else -> Color(0xFFE5E7EB) }), Alignment.Center) {
                Text("$rank", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold, color = if (rank <= 3) Color.White else NightBlack)
            }
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(county.code, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = DeepBlue)
                    Spacer(Modifier.width(6.dp))
                    Text(county.name, style = MaterialTheme.typography.bodySmall, color = NightBlack, fontWeight = FontWeight.Medium)
                }
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(progress = { anim }, modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp)), color = color, trackColor = Color(0xFFE5E7EB))
                Spacer(Modifier.height(2.dp))
                Text("${nf.format(county.votesCast)} / ${nf.format(county.registeredVoters)}", style = MaterialTheme.typography.labelSmall, color = Steel300, fontSize = 9.sp)
            }
            Spacer(Modifier.width(10.dp))
            Text(String.format("%.1f%%", county.turnoutPercent), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = color)
        }
    }
}
