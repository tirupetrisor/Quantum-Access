package com.example.quantumaccess.feature.guide.presentation

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
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.GppGood
import androidx.compose.material.icons.filled.HowToVote
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.quantumaccess.core.designsystem.theme.DeepBlue
import com.example.quantumaccess.core.designsystem.theme.Emerald
import com.example.quantumaccess.core.designsystem.theme.NightBlack
import com.example.quantumaccess.core.designsystem.theme.Slate800

@Composable
fun VotingGuideScreen(onBack: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8F9FC))
            .navigationBarsPadding()
    ) {
        Column(Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DeepBlue)
                    .statusBarsPadding()
                    .padding(horizontal = 8.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = null, tint = Color.White)
                }
                Spacer(Modifier.width(4.dp))
                Column {
                    Text("Ghid de votare", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("Pasii necesari pentru a vota", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp)
            ) {
                // Intro card
                Card(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(16.dp),
                    CardDefaults.cardColors(DeepBlue.copy(alpha = 0.06f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.Info, null, tint = DeepBlue, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Despre procesul de votare", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(
                            "Votul tau este protejat prin criptare quantum (QKD - BB84). Fiecare vot este criptat cu o cheie generata prin distributie cuantica de chei, facand interceptarea imposibila conform legilor fizicii cuantice.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Slate800
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Text("Pasi de urmat", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                Spacer(Modifier.height(16.dp))

                // Steps
                StepCard(
                    number = 1,
                    icon = Icons.Filled.Badge,
                    title = "Prezinta-te la sectia de votare",
                    description = "Vino la sectia de votare cu cartea de identitate valabila (CI/BI). Verifica pe lista de la intrare numarul sectiei tale.",
                    color = Color(0xFF6366F1)
                )
                StepConnector()
                StepCard(
                    number = 2,
                    icon = Icons.Filled.Face,
                    title = "Verificare identitate digitala",
                    description = "Introdu CNP-ul, fotografiaza buletinul si fa un selfie. ML Kit Face Detection compara fotografia de pe CI cu selfie-ul tau in timp real.",
                    color = Color(0xFF0EA5E9)
                )
                StepConnector()
                StepCard(
                    number = 3,
                    icon = Icons.Filled.LocationOn,
                    title = "Confirmare locatie GPS",
                    description = "Aplicatia verifica prin GPS ca esti prezent fizic la sectia de votare. Aceasta previne votul de la distanta neautorizat.",
                    color = Color(0xFFF59E0B)
                )
                StepConnector()
                StepCard(
                    number = 4,
                    icon = Icons.Filled.HowToVote,
                    title = "Alege si voteaza",
                    description = "Selecteaza alegerea activa, alege candidatul dorit si confirma votul. O cheie QKD unica este generata pentru criptarea votului tau.",
                    color = Emerald
                )
                StepConnector()
                StepCard(
                    number = 5,
                    icon = Icons.Filled.Receipt,
                    title = "Primeste chitanta verificabila",
                    description = "Dupa votare primesti o chitanta criptata cu un token unic. Poti verifica ulterior ca votul a fost inregistrat corect, fara a dezvalui optiunea.",
                    color = Color(0xFF8B5CF6)
                )

                Spacer(Modifier.height(24.dp))

                // Security info
                Card(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(16.dp),
                    CardDefaults.cardColors(Emerald.copy(alpha = 0.08f))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.GppGood, null, tint = Emerald, modifier = Modifier.size(20.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Securitate quantum", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        }
                        Spacer(Modifier.height(10.dp))
                        SecurityItem("Protocolul BB84 genereaza chei prin stari cuantice ale fotonilor")
                        SecurityItem("Orice tentativa de interceptare (Eve) este detectata automat prin QBER")
                        SecurityItem("Votul tau nu poate fi decriptat, modificat sau duplicat")
                        SecurityItem("Chitanta verificabila fara a compromite secretul votului")
                    }
                }

                Spacer(Modifier.height(16.dp))

                // What to bring
                Card(
                    Modifier.fillMaxWidth(),
                    RoundedCornerShape(16.dp),
                    CardDefaults.cardColors(Color(0xFFFFF7ED))
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Ce trebuie sa aduci:", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                        Spacer(Modifier.height(8.dp))
                        BringItem("Carte de identitate valabila (CI)")
                        BringItem("Telefon cu aplicatia QuantumAccess instalata")
                        BringItem("Conexiune la internet (WiFi sau date mobile)")
                    }
                }

                Spacer(Modifier.height(24.dp))
            }
        }
    }
}

@Composable
private fun StepCard(number: Int, icon: ImageVector, title: String, description: String, color: Color) {
    Card(
        Modifier.fillMaxWidth(),
        RoundedCornerShape(16.dp),
        CardDefaults.cardColors(Color.White),
        CardDefaults.cardElevation(2.dp)
    ) {
        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(40.dp).clip(CircleShape).background(color),
                Alignment.Center
            ) {
                Text("$number", fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
            }
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(icon, null, tint = color, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(title, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = NightBlack)
                }
                Spacer(Modifier.height(6.dp))
                Text(description, style = MaterialTheme.typography.bodySmall, color = Slate800)
            }
        }
    }
}

@Composable
private fun StepConnector() {
    Box(
        Modifier
            .padding(start = 38.dp)
            .size(2.dp, 20.dp)
            .background(Color(0xFFE5E7EB))
    )
}

@Composable
private fun SecurityItem(text: String) {
    Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.Top) {
        Icon(Icons.Filled.CheckCircle, null, tint = Emerald, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodySmall, color = NightBlack)
    }
}

@Composable
private fun BringItem(text: String) {
    Row(Modifier.padding(vertical = 3.dp)) {
        Text("  •  ", color = Color(0xFFF59E0B), fontWeight = FontWeight.Bold)
        Text(text, style = MaterialTheme.typography.bodySmall, color = NightBlack)
    }
}
