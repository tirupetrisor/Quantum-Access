package com.example.quantumaccess.feature.elections.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Emerald
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate800
import com.example.quantumaccess.domain.model.Election
import com.example.quantumaccess.domain.model.ElectionType

@Composable
fun ElectionsListScreen(
    elections: List<Election>,
    isLoading: Boolean,
    onElectionClick: (Election) -> Unit,
    onBack: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBlue)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Înapoi", tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(4.dp))
                    Column {
                        Text(
                            "Alegeri disponibile",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            "Secția #42 · Selectează o alegere pentru a vota",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = DeepBlue)
                }
            } else if (elections.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize().padding(32.dp), contentAlignment = Alignment.Center) {
                    Text(
                        "Nu sunt alegeri active momentan.\nRevin-o mai târziu.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = Slate800,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(20.dp)
                ) {
                    item {
                        Text(
                            "Votul tău este criptat end-to-end cu QKD.\nNimeni nu poate intercepta sau modifica votul.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate800,
                            lineHeight = 18.sp,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(elections, key = { it.id }) { election ->
                        ElectionCard(election = election, onClick = { onElectionClick(election) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ElectionCard(election: Election, onClick: () -> Unit) {
    val (typeLabel, typeIcon, accentColor) = when (election.type) {
        ElectionType.PRESIDENTIAL -> Triple("Alegeri Prezidențiale", Icons.Filled.Person, Color(0xFF6366F1))
        ElectionType.PARLIAMENTARY -> Triple("Alegeri Parlamentare", Icons.Filled.Groups, Color(0xFF0EA5E9))
        ElectionType.LOCAL -> Triple("Alegeri Locale", Icons.Filled.LocationCity, Color(0xFFF59E0B))
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(typeIcon, contentDescription = null, tint = accentColor, modifier = Modifier.size(28.dp))
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    election.displayName(localeRo = true),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = NightBlack
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(typeLabel, style = MaterialTheme.typography.bodySmall, color = Slate800)
                Spacer(modifier = Modifier.height(6.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (election.isActive) {
                        Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(Emerald))
                        Spacer(modifier = Modifier.width(5.dp))
                        Text("Vot deschis", style = MaterialTheme.typography.labelSmall, color = Emerald, fontWeight = FontWeight.Medium)
                        Spacer(modifier = Modifier.width(12.dp))
                    }
                    Text(
                        "${election.options.size} candidați/opțiuni",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF9CA3AF)
                    )
                }
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = Color(0xFFD1D5DB))
        }
    }
}
