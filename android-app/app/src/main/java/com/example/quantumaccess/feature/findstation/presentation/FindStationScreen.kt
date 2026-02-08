package com.example.quantumaccess.feature.findstation.presentation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
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
import androidx.compose.material.icons.filled.DirectionsWalk
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
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

// ─── Data ──────────────────────────────────────────────────────────────────────

private data class NearbyStation(
    val id: Int,
    val name: String,
    val address: String,
    val distanceMeters: Int,
    val walkMinutes: Int,
    val queueSize: Int,
    val isAssigned: Boolean
)

private val nearbyStations = listOf(
    NearbyStation(
        id = 43,
        name = "Liceul \"Grigore Moisil\"",
        address = "Piata Balcescu 1, Timisoara",
        distanceMeters = 0,
        walkMinutes = 0,
        queueSize = 8,
        isAssigned = true
    ),
    NearbyStation(
        id = 44,
        name = "Colegiul National \"C.D. Loga\"",
        address = "Bd. C.D. Loga 37, Timisoara",
        distanceMeters = 350,
        walkMinutes = 5,
        queueSize = 11,
        isAssigned = false
    ),
    NearbyStation(
        id = 47,
        name = "Liceul Teoretic \"J.L. Calderon\"",
        address = "Str. Gh. Doja 47, Timisoara",
        distanceMeters = 620,
        walkMinutes = 8,
        queueSize = 6,
        isAssigned = false
    ),
    NearbyStation(
        id = 51,
        name = "Scoala Gimnaziala nr. 7 \"Domokos Kazmer\"",
        address = "Str. Brancoveanu 4, Timisoara",
        distanceMeters = 900,
        walkMinutes = 12,
        queueSize = 14,
        isAssigned = false
    ),
    NearbyStation(
        id = 56,
        name = "Universitatea de Vest",
        address = "Bd. V. Parvan 4, Timisoara",
        distanceMeters = 1300,
        walkMinutes = 17,
        queueSize = 22,
        isAssigned = false
    ),
    NearbyStation(
        id = 62,
        name = "Colegiul National Banatean",
        address = "Bd. 16 Decembrie 1989 nr. 26, Timisoara",
        distanceMeters = 1800,
        walkMinutes = 23,
        queueSize = 9,
        isAssigned = false
    )
)

// ─── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun FindStationScreen(onBack: () -> Unit, onNavigateToStation: () -> Unit = {}) {
    var isLocating by remember { mutableStateOf(true) }
    var showStations by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(1800) // Simulate GPS acquisition
        isLocating = false
        delay(300)
        showStations = true
    }

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
                            Text("Gaseste sectia ta", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("Sectii de votare din apropierea ta", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                        }
                        Icon(Icons.Filled.MyLocation, null, tint = if (isLocating) Color.White.copy(alpha = 0.5f) else Emerald, modifier = Modifier.size(22.dp))
                        Spacer(Modifier.width(12.dp))
                    }

                    Spacer(Modifier.height(16.dp))

                    // GPS Status
                    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (isLocating) {
                            CircularProgressIndicator(color = Color.White, modifier = Modifier.size(32.dp), strokeWidth = 3.dp)
                            Spacer(Modifier.height(8.dp))
                            Text("Se detecteaza locatia GPS...", style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.7f))
                        } else {
                            Icon(Icons.Filled.GpsFixed, null, tint = Emerald, modifier = Modifier.size(32.dp))
                            Spacer(Modifier.height(6.dp))
                            Text("Locatie detectata", style = MaterialTheme.typography.bodySmall, color = Emerald, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.height(2.dp))
                            Text("45.7489° N, 21.2087° E · Timisoara, jud. Timis", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.5f))
                        }
                    }
                }
            }

            // Content
            AnimatedVisibility(
                visible = showStations,
                enter = fadeIn(tween(400)) + slideInVertically(tween(400)) { 30 }
            ) {
                Column(
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                ) {
                    // Assigned station banner
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(16.dp),
                        CardDefaults.cardColors(Emerald.copy(alpha = 0.08f)),
                        CardDefaults.cardElevation(0.dp)
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Filled.Star, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sectia ta arondata", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Conform CNP-ului tau, esti arondat la sectia de mai jos. Poti vota doar la sectia ta.",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate800
                            )
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    // Station cards
                    nearbyStations.forEach { station ->
                        StationCard(station = station, onNavigate = onNavigateToStation)
                        Spacer(Modifier.height(10.dp))
                    }

                    Spacer(Modifier.height(8.dp))

                    // Info card
                    Card(
                        Modifier.fillMaxWidth(),
                        RoundedCornerShape(12.dp),
                        CardDefaults.cardColors(Color(0xFFFFF7ED))
                    ) {
                        Column(Modifier.padding(14.dp)) {
                            Text("Informatii importante", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = Color(0xFFB45309))
                            Spacer(Modifier.height(6.dp))
                            InfoItem("Votul se realizeaza doar la sectia ta arondata")
                            InfoItem("Sectiile sunt deschise intre orele 07:00 - 21:00")
                            InfoItem("Ai nevoie de carte de identitate valabila")
                            InfoItem("Verificarea se face prin ML Kit + QKD la sectie")
                        }
                    }

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}

// ─── Station Card ──────────────────────────────────────────────────────────────

@Composable
private fun StationCard(station: NearbyStation, onNavigate: () -> Unit) {
    val isAssigned = station.isAssigned
    Card(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(16.dp),
        CardDefaults.cardColors(Color.White),
        CardDefaults.cardElevation(if (isAssigned) 4.dp else 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Station icon
                Box(
                    Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(if (isAssigned) DeepBlue else Color(0xFFE5E7EB)),
                    Alignment.Center
                ) {
                    if (isAssigned) {
                        Icon(Icons.Filled.Star, null, tint = Color(0xFFFBBF24), modifier = Modifier.size(20.dp))
                    } else {
                        Icon(Icons.Filled.LocationOn, null, tint = Slate800, modifier = Modifier.size(20.dp))
                    }
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "Sectia nr. ${station.id}",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = if (isAssigned) DeepBlue else NightBlack
                        )
                        if (isAssigned) {
                            Spacer(Modifier.width(8.dp))
                            Box(
                                Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Emerald.copy(alpha = 0.15f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("SECTIA TA", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Emerald)
                            }
                        }
                    }
                    Text(station.name, style = MaterialTheme.typography.bodySmall, color = NightBlack, fontWeight = FontWeight.Medium)
                    Text(station.address, style = MaterialTheme.typography.labelSmall, color = Slate800)
                }
            }

            Spacer(Modifier.height(12.dp))

            // Stats row
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                StationStat(
                    Icons.Filled.NearMe,
                    if (station.distanceMeters == 0) "Aici" else if (station.distanceMeters < 1000) "${station.distanceMeters}m" else String.format("%.1f km", station.distanceMeters / 1000f),
                    "Distanta",
                    if (station.distanceMeters == 0) Emerald else DeepBlue
                )
                StationStat(
                    Icons.Filled.DirectionsWalk,
                    if (station.walkMinutes == 0) "—" else "${station.walkMinutes} min",
                    "Pe jos",
                    Color(0xFF6366F1)
                )
                StationStat(
                    Icons.Filled.Groups,
                    "${station.queueSize}",
                    "In coada",
                    if (station.queueSize > 10) Color(0xFFF59E0B) else Emerald
                )
                StationStat(
                    Icons.Filled.Schedule,
                    if (station.queueSize > 10) "~${station.queueSize * 2} min" else "~${station.queueSize * 2} min",
                    "Asteptare",
                    Steel300
                )
            }

            if (isAssigned) {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = onNavigate,
                    Modifier.fillMaxWidth().height(44.dp),
                    colors = ButtonDefaults.buttonColors(DeepBlue),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Filled.Navigation, null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Navigheaza la sectie", fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                }
            }
        }
    }
}

@Composable
private fun StationStat(icon: ImageVector, value: String, label: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(Modifier.height(4.dp))
        Text(value, style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = NightBlack)
        Text(label, fontSize = 9.sp, color = Slate800)
    }
}

@Composable
private fun InfoItem(text: String) {
    Row(Modifier.padding(vertical = 2.dp)) {
        Text("  •  ", color = Color(0xFFB45309), fontWeight = FontWeight.Bold, fontSize = 12.sp)
        Text(text, style = MaterialTheme.typography.bodySmall, color = Color(0xFF92400E))
    }
}
