package com.example.quantumaccess.feature.dashboard.presentation

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.NearMe
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.example.quantumaccess.core.designsystem.components.QuantumTopBar
import com.example.quantumaccess.core.designsystem.theme.BorderLight
import com.example.quantumaccess.core.designsystem.theme.Cloud200
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Emerald
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate700
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.core.designsystem.theme.Steel200
import com.example.quantumaccess.core.designsystem.theme.Steel300
import com.example.quantumaccess.core.util.findActivity

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun DashboardScreen(
    modifier: Modifier = Modifier,
    onOpenElections: () -> Unit = {},
    onOpenStatistics: () -> Unit = {},
    onOpenVotingGuide: () -> Unit = {},
    onOpenFindStation: () -> Unit = {},
    onOpenResults: () -> Unit = {},
    onInitiateTransaction: () -> Unit = {},
    onOpenHistory: () -> Unit = {},
    onOpenAnalytics: () -> Unit = {},
    onLogoutConfirm: () -> Unit = {}
) {
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

    // Simulated: false = at home, true = at station
    // In production, this would be a real GPS check
    var isAtStation by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.White)
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuantumTopBar(
                title = "QuantumAccess",
                subtitle = "Unlock the Unknown",
                showLogoutButton = true,
                onLogoutClick = onLogoutConfirm
            )
            Column(
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Top
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Dynamic location status
                LocationStatusCard(
                    isAtStation = isAtStation,
                    onToggleDemo = { isAtStation = !isAtStation }
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 1. Vote (available only at station)
                DashboardCard(
                    title = if (isAtStation) "Voteaza acum" else "Voteaza acum",
                    subtitle = if (isAtStation) "Esti la sectie — voteaza securizat cu criptare QKD"
                              else "Disponibil doar la sectia de votare",
                    leading = {
                        IconBubble(Color.White, if (isAtStation) DeepBlue else Color(0xFF9CA3AF)) {
                            Icon(Icons.Filled.HowToVote, null, tint = Color.White)
                        }
                    },
                    onClick = if (isAtStation) onOpenElections else onOpenFindStation,
                    borderColor = if (isAtStation) DeepBlue.copy(alpha = 0.3f) else Color(0xFFE5E7EB),
                    highlight = isAtStation
                )
                Spacer(modifier = Modifier.height(14.dp))

                // 2. Live statistics (works from anywhere)
                DashboardCard(
                    title = "Statistici live",
                    subtitle = "Prezenta la vot pe judete, harta Romaniei in timp real",
                    leading = {
                        IconBubble(Color.White, Color(0xFF6366F1)) {
                            Icon(Icons.Filled.BarChart, null, tint = Color.White)
                        }
                    },
                    onClick = onOpenStatistics,
                    borderColor = Color(0xFF6366F1).copy(alpha = 0.3f),
                    highlight = true
                )
                Spacer(modifier = Modifier.height(14.dp))

                // 3. Find your station (GPS)
                DashboardCard(
                    title = "Gaseste sectia ta",
                    subtitle = "Localizeaza cea mai apropiata sectie de votare prin GPS",
                    leading = {
                        IconBubble(Color.White, Emerald) {
                            Icon(Icons.Filled.NearMe, null, tint = Color.White)
                        }
                    },
                    onClick = onOpenFindStation,
                    borderColor = Emerald.copy(alpha = 0.3f),
                    highlight = true
                )
                Spacer(modifier = Modifier.height(14.dp))

                // 4. Voting guide (works from anywhere)
                DashboardCard(
                    title = "Ghid de votare",
                    subtitle = "Pasi, documente necesare si informatii utile",
                    leading = {
                        IconBubble(Slate700, Cloud200) {
                            Icon(Icons.Filled.MenuBook, null, tint = Slate700)
                        }
                    },
                    onClick = onOpenVotingGuide,
                    borderColor = BorderLight
                )
                Spacer(modifier = Modifier.height(14.dp))

                // 5. Results (locked until 21:00)
                DashboardCard(
                    title = "Rezultate alegeri",
                    subtitle = "Disponibile dupa ora 21:00 · Numaratoare oficiala",
                    leading = {
                        IconBubble(Color.White, Color(0xFFF59E0B)) {
                            Icon(Icons.Filled.EmojiEvents, null, tint = Color.White)
                        }
                    },
                    onClick = onOpenResults,
                    borderColor = Color(0xFFF59E0B).copy(alpha = 0.3f),
                    highlight = true
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
            Footer()
        }
    }
}

@Composable
private fun LocationStatusCard(isAtStation: Boolean, onToggleDemo: () -> Unit) {
    if (isAtStation) {
        // At station
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Emerald.copy(alpha = 0.06f)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Emerald.copy(alpha = 0.2f))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.GpsFixed, null, tint = Emerald, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Esti la sectia de votare", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = Emerald)
                }
                Spacer(Modifier.height(6.dp))
                Text("Sectia nr. 43 · Liceul \"Grigore Moisil\"\nPiata Balcescu 1, Timisoara, jud. Timis", style = MaterialTheme.typography.bodySmall, color = Slate800)
                Spacer(Modifier.height(10.dp))
                StatusRow(Emerald, "Identitate verificata · ML Kit Face Match OK")
                Spacer(Modifier.height(4.dp))
                StatusRow(Emerald, "Locatie GPS confirmata la sectie")
                Spacer(Modifier.height(4.dp))
                StatusRow(Emerald, "Canal QKD activ · BB84 · Retea quantum securizata")
                Spacer(Modifier.height(8.dp))
                // Demo toggle
                Text("Demo: apasa pentru a simula 'acasa'", style = MaterialTheme.typography.labelSmall, color = Steel300,
                    modifier = Modifier.clickable { onToggleDemo() })
            }
        }
    } else {
        // At home
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF7ED)),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFDE68A))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.LocationOn, null, tint = Color(0xFFF59E0B), modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text("Nu esti la sectia de votare", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        Text("Foloseste app-ul de acasa pentru informatii", style = MaterialTheme.typography.labelSmall, color = Slate800)
                    }
                }
                Spacer(Modifier.height(10.dp))
                StatusRow(Emerald, "Identitate verificata · ML Kit Face Match OK")
                Spacer(Modifier.height(4.dp))
                StatusRow(Color(0xFFF59E0B), "GPS: Nu esti in raza unei sectii de votare")
                Spacer(Modifier.height(4.dp))
                StatusRow(Emerald, "Statistici, ghid si gasire sectie — disponibile")
                Spacer(Modifier.height(4.dp))
                StatusRow(Color(0xFFEF4444), "Votare — necesita prezenta la sectie")
                Spacer(Modifier.height(8.dp))
                // Demo toggle
                Text("Demo: apasa pentru a simula 'la sectie'", style = MaterialTheme.typography.labelSmall, color = Steel300,
                    modifier = Modifier.clickable { onToggleDemo() })
            }
        }
    }
}

@Composable
private fun StatusRow(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(CircleShape).background(color))
        Spacer(Modifier.width(6.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, color = color)
    }
}

@Composable
private fun DashboardCard(title: String, subtitle: String, leading: @Composable () -> Unit, onClick: () -> Unit, borderColor: Color, highlight: Boolean = false) {
    val src = remember { MutableInteractionSource() }
    val pressed by src.collectIsPressedAsState()
    val scale by animateFloatAsState(if (pressed) 0.97f else 1f, label = "s")
    Surface(
        shape = RoundedCornerShape(18.dp),
        shadowElevation = if (highlight) 8.dp else 4.dp,
        color = Color.White,
        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).scale(scale).clickable(src, null) { onClick() },
        tonalElevation = 0.dp,
        border = androidx.compose.foundation.BorderStroke(if (highlight) 2.dp else 1.dp, borderColor)
    ) {
        Row(Modifier.fillMaxWidth().padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(Modifier.weight(1f), verticalAlignment = Alignment.CenterVertically) {
                leading()
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = NightBlack, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.height(3.dp))
                    Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Slate800)
                }
            }
            Icon(Icons.Filled.ChevronRight, null, tint = Steel200)
        }
    }
}

@Composable
private fun IconBubble(iconTint: Color, background: Color, content: @Composable () -> Unit) {
    Box(Modifier.size(44.dp).clip(CircleShape).background(background), Alignment.Center) { content() }
}

@Composable
private fun Footer() {
    Column(Modifier.fillMaxWidth().padding(bottom = 16.dp, top = 8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.Verified, null, tint = Emerald, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text("Quantum Network Active", color = Slate800, style = MaterialTheme.typography.labelSmall)
        }
        Spacer(Modifier.height(4.dp))
        Text("Vot criptat cu QKD · BB84 · Disponibil acasa si la sectie", color = Steel300, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center)
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
private fun DashboardPreview() {
    Surface(color = Color.White) { Column { DashboardScreen() } }
}
